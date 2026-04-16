package com.stockgame.service;

import com.stockgame.domain.entity.Stock;
import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizScheduleService {

    private final StockRepository stockRepository;
    private final StockQuizRepository quizRepository;
    private final StockScraper stockScraper;
    private final MarketHolidayRepository holidayRepository;

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void createDailyQuiz() {
        LocalDate today = LocalDate.now();

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            log.info("오늘({})은 주말입니다. 퀴즈를 생성하지 않습니다.", today);
            return;
        }

        if (holidayRepository.existsByHolidayDate(today)) {
            log.info("오늘({})은 휴장일입니다. 퀴즈를 생성하지 않습니다.", today);
            return;
        }

        if (quizRepository.findByQuizDate(today).isPresent()) {
            log.info("오늘의 퀴즈가 이미 존재합니다. 생성을 건너뜁니다.");
            return;
        }

        List<Stock> activeStocks = stockRepository.findByIsActiveTrueOrderByStockCodeAsc();
        if (activeStocks.isEmpty()) {
            log.warn("활성화된 종목이 없습니다. 퀴즈를 생성할 수 없습니다.");
            return;
        }

        Stock selectedStock = activeStocks.get(0);

        try {
            BigDecimal currentPrice = stockScraper.getStockPrice(selectedStock.getStockCode());

            StockQuizDaily todayQuiz = StockQuizDaily.builder()
                    .quizDate(today)
                    .stock(selectedStock)
                    .base_price(currentPrice)
                    .status("OPEN")
                    .build();

            quizRepository.save(todayQuiz);
            log.info("일일 퀴즈 생성 완료: {} ({}) 기준가={}",
                    selectedStock.getStockName(), selectedStock.getStockCode(), currentPrice);
        } catch (Exception e) {
            log.error("퀴즈 생성 실패: {}", e.getMessage(), e);
        }
    }
}
