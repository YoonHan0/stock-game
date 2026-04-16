package com.stockgame.domain.repository;

import com.stockgame.domain.entity.MarketHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MarketHolidayRepository extends JpaRepository<MarketHoliday, Long> {

    boolean existsByHolidayDate(LocalDate date);
}
