package com.stockgame.domain.repository;

import com.stockgame.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByUserIdAndQuizQuizId(Long userId, Long quizId);

    // 특정 퀴즈의 예측값 별로 개수를 세는 메서드
    long countByQuizQuizIdAndPrediction(Long quizId, String prediction);
}