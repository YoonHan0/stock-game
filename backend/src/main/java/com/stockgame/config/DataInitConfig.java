package com.stockgame.config;

import com.stockgame.domain.entity.Stock;
import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.StockRepository;
import com.stockgame.service.StockScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig {

    private final StockRepository stockRepository;
    private final StockQuizRepository quizRepository;
    private final StockScraper stockScraper;

    @Bean
    public CommandLineRunner initData() {

        log.info("=== 초기 데이터 생성 시작 ===");

        return args -> {
            // 1. 기본 종목 데이터 (삼성전자) 생성
            String samsungCode = "005930";
            if (!stockRepository.existsById(samsungCode)) {
                Stock samsung = Stock.builder()
                        .stockCode(samsungCode)
                        .stockName("삼성전자")
                        .isActive(true)
                        .build();
                stockRepository.save(samsung);
                log.info("초기 데이터: 삼성전자 등록 완료");
            }

            // 2. 오늘의 퀴즈 데이터 생성 (없을 경우에만)
            LocalDate today = LocalDate.now();
            if (quizRepository.findByQuizDate(today).isEmpty()) {
                Stock samsung = stockRepository.findById(samsungCode).orElseThrow();

                // 스크래퍼를 이용해 현재가(기준가) 가져오기
                BigDecimal currentPrice = stockScraper.getStockPrice(samsungCode);

                StockQuizDaily todayQuiz = StockQuizDaily.builder()
                        .quizDate(today)
                        .stock(samsung)
                        .base_price(currentPrice)
                        .status("OPEN")
                        .build();

                quizRepository.save(todayQuiz);
                log.info("초기 데이터: {} 퀴즈 생성 완료 (기준가: {})", today, currentPrice);
            }
        };
    }
}