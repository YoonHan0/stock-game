package com.stockgame.service;

import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.entity.Vote;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.VoteRepository;
import com.stockgame.dto.VoteRequestDto;
import com.stockgame.dto.VoteStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StockQuizRepository quizRepository;
    private final StockScraper stockScraper;
    private final VoteRepository voteRepository;

    @Transactional(readOnly = true)
    public StockQuizDaily getTodayQuiz() {
        LocalDate today = LocalDate.now();
        // 오늘 날짜의 퀴즈가 DB에 있는지 확인
        return quizRepository.findByQuizDate(today)
                .orElseThrow(() -> new RuntimeException("오늘의 퀴즈가 생성되지 않았습니다."));
    }

    // 현재가를 실시간으로 반영하여 정보를 내려주는 로직
    public BigDecimal getCurrentLivePrice(String stockCode) {
        return stockScraper.getStockPrice(stockCode);
    }

    // 장마감 여부 판단 (오후 4시 기준)
    public boolean isMarketClosed() {
        return LocalTime.now().isAfter(LocalTime.of(16, 0));
    }

    @Transactional
    public void saveVote(VoteRequestDto dto) {
        StockQuizDaily quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));

        // String을 UUID로 변환 (표준 UUID 형식이 아니어도 해시값 기반으로 생성해줌)
        UUID userUuid = UUID.nameUUIDFromBytes(dto.getUserId().getBytes());

        Vote vote = Vote.builder()
                .quiz(quiz)
                .userId(userUuid)
                .prediction(dto.getPrediction())
                .build();

        voteRepository.save(vote);
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

    public boolean getIsVoteState(Long quizId, String userId) {

        UUID userUUID = UUID.nameUUIDFromBytes(userId.getBytes());

        return voteRepository.existsByUserIdAndQuizQuizId(userUUID, quizId);
    }
}