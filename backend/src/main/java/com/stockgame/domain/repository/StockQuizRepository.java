package com.stockgame.domain.repository;

import com.stockgame.domain.entity.StockQuizDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface StockQuizRepository extends JpaRepository<StockQuizDaily, Long> {
    Optional<StockQuizDaily> findByQuizDate(LocalDate date);
}