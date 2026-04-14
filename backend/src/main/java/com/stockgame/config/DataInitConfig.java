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
import java.util.List;

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
            LocalDate today = LocalDate.now();

            if (quizRepository.findByQuizDate(today).isPresent()) {
                log.info("오늘의 퀴즈가 이미 존재합니다. 초기화를 건너뜁니다.");
                return;
            }

            List<Stock> activeStocks = stockRepository.findByIsActiveTrueOrderByStockCodeAsc();
            if (activeStocks.isEmpty()) {
                log.warn("활성화된 종목이 없습니다. 퀴즈를 생성할 수 없습니다.");
                return;
            }

            // is_active=true 종목 중 stock_code가 가장 작은 종목 선택
            Stock selectedStock = activeStocks.get(0);

            BigDecimal currentPrice = stockScraper.getStockPrice(selectedStock.getStockCode());

            StockQuizDaily todayQuiz = StockQuizDaily.builder()
                    .quizDate(today)
                    .stock(selectedStock)
                    .base_price(currentPrice)
                    .status("OPEN")
                    .build();

            quizRepository.save(todayQuiz);
            log.info("오늘의 퀴즈 생성 완료: {} ({}) 기준가={}", selectedStock.getStockName(), selectedStock.getStockCode(), currentPrice);
        };
    }
}