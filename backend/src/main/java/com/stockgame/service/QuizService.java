package com.stockgame.service;

import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.entity.Vote;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.domain.repository.VoteRepository;
import com.stockgame.dto.VoteRequestDto;
import com.stockgame.dto.VoteStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final LocalTime VOTE_START = LocalTime.of(9, 0);
    private static final LocalTime VOTE_END = LocalTime.of(20, 0);

    private final StockQuizRepository quizRepository;
    private final StockScraper stockScraper;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public StockQuizDaily getTodayQuiz() {
        LocalDate today = LocalDate.now();
        return quizRepository.findByQuizDate(today)
                .orElseThrow(() -> new RuntimeException("오늘의 퀴즈가 생성되지 않았습니다."));
    }

    public BigDecimal getCurrentLivePrice(String stockCode) {
        return stockScraper.getStockPrice(stockCode);
    }

    public boolean isMarketClosed() {
        return LocalTime.now().isAfter(LocalTime.of(20, 0));
    }

    @Transactional
    public void saveVote(VoteRequestDto dto, Object principal) {
        Long userId = resolveUserId(principal);

        LocalTime now = LocalTime.now();
        if (now.isBefore(VOTE_START) || now.isAfter(VOTE_END)) {
            throw new QuizVoteConflictException(
                    "VOTE_TIME_RESTRICTED",
                    "투표는 오전 9시부터 오후 8시까지 가능합니다.");
        }

        StockQuizDaily quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다. quizId=" + dto.getQuizId()));

        if (!quiz.getQuizDate().equals(LocalDate.now())) {
            throw new QuizVoteConflictException(
                    "QUIZ_CLOSED",
                    "오늘의 퀴즈가 아닙니다.");
        }

        if (!"OPEN".equals(quiz.getStatus())) {
            throw new QuizVoteConflictException(
                    "QUIZ_CLOSED",
                    "이미 마감된 퀴즈입니다.");
        }

        if (voteRepository.existsByUserIdAndQuizQuizId(userId, dto.getQuizId())) {
            throw new QuizVoteConflictException(
                    "ALREADY_VOTED",
                    "이미 투표한 퀴즈입니다.");
        }

        Vote vote = Vote.builder()
                .quiz(quiz)
                .userId(userId)
                .prediction(dto.getPrediction())
                .build();

        voteRepository.save(vote);
    }

    private Long resolveUserId(Object principal) {
        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof String email && StringUtils.hasText(email)) {
            return userRepository.findByEmail(email)
                    .map(u -> u.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "인증 정보로 사용자를 찾을 수 없습니다. email=" + email));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }

    public VoteStatsDto getVoteStats(Long quizId) {
        long up = voteRepository.countByQuizQuizIdAndPrediction(quizId, "UP");
        long down = voteRepository.countByQuizQuizIdAndPrediction(quizId, "DOWN");
        long total = up + down;

        if (total == 0) return new VoteStatsDto(0, 0, 0, 0);

        double upPercent = Math.round((double) up / total * 1000) / 10.0;
        double downPercent = 100.0 - upPercent;

        return new VoteStatsDto(up, down, upPercent, downPercent);
    }

    public boolean getIsVoteState(Long quizId, Object principal) {
        Long userId = resolveUserId(principal);
        return voteRepository.existsByUserIdAndQuizQuizId(userId, quizId);
    }
}
