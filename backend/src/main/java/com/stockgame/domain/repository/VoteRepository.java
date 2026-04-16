package com.stockgame.domain.repository;

import com.stockgame.domain.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByUserIdAndQuizQuizId(Long userId, Long quizId);

    long countByQuizQuizIdAndPrediction(Long quizId, String prediction);

    List<Vote> findByQuizQuizId(Long quizId);

    Page<Vote> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndIsCorrectTrue(Long userId);
}