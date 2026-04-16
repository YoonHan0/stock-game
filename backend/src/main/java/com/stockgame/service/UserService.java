package com.stockgame.service;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.entity.Vote;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.domain.repository.VoteRepository;
import com.stockgame.dto.UserProfileDto;
import com.stockgame.dto.VoteHistoryItemDto;
import com.stockgame.dto.VoteHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Object principal) {
        User user = resolveUser(principal);

        long totalVotes = voteRepository.countByUserId(user.getId());
        long correctVotes = voteRepository.countByUserIdAndIsCorrectTrue(user.getId());
        double accuracy = totalVotes == 0 ? 0.0
                : Math.round((double) correctVotes / totalVotes * 1000) / 10.0;

        return UserProfileDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .points(user.getPoints())
                .totalVotes(totalVotes)
                .correctVotes(correctVotes)
                .accuracy(accuracy)
                .build();
    }

    @Transactional(readOnly = true)
    public VoteHistoryResponseDto getVoteHistory(Object principal, int page, int size) {
        User user = resolveUser(principal);

        Page<Vote> votePage = voteRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size));

        var items = votePage.getContent().stream().map(vote -> {
            var quiz = vote.getQuiz();
            String settlementStatus = "CLOSED".equals(quiz.getStatus()) ? "정산 완료" : "정산 대기";

            return VoteHistoryItemDto.builder()
                    .quizId(quiz.getQuizId())
                    .quizDate(quiz.getQuizDate())
                    .stockName(quiz.getStock().getStockName())
                    .stockCode(quiz.getStock().getStockCode())
                    .prediction(vote.getPrediction())
                    .isCorrect(vote.getIsCorrect())
                    .pointsEarned(vote.getPointsEarned())
                    .quizResult(quiz.getQuizResult())
                    .settlementStatus(settlementStatus)
                    .build();
        }).toList();

        return VoteHistoryResponseDto.builder()
                .items(items)
                .page(page)
                .totalPages(votePage.getTotalPages())
                .totalItems(votePage.getTotalElements())
                .build();
    }

    private User resolveUser(Object principal) {
        if (principal instanceof Long id) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        }
        if (principal instanceof String email && StringUtils.hasText(email)) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "인증 정보로 사용자를 찾을 수 없습니다. email=" + email));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
}
