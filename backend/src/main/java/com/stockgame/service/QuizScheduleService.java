package com.stockgame.service;

import com.stockgame.domain.entity.Stock;
import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.StockRepository;
import com.stockgame.dto.ScheduleTriggerResponseDto;
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
        ScheduleTriggerResponseDto result = triggerCreateDailyQuiz();
        log.info("[{}] {}", result.action(), result.message());
    }

    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Seoul")
    @Transactional
    public void closeTodayQuiz() {
        ScheduleTriggerResponseDto result = triggerCloseTodayQuiz();
        log.info("[{}] {}", result.action(), result.message());
    }

    @Transactional
    public ScheduleTriggerResponseDto triggerCreateDailyQuiz() {
        LocalDate today = currentDate();

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return new ScheduleTriggerResponseDto(
                    "create-quiz",
                    false,
                    0,
                    "오늘(" + today + ")은 주말이라 퀴즈를 생성하지 않습니다.");
        }

        if (holidayRepository.existsByHolidayDate(today)) {
            return new ScheduleTriggerResponseDto(
                    "create-quiz",
                    false,
                    0,
                    "오늘(" + today + ")은 휴장일이라 퀴즈를 생성하지 않습니다.");
        }

        if (quizRepository.findByQuizDate(today).isPresent()) {
            return new ScheduleTriggerResponseDto(
                    "create-quiz",
                    false,
                    0,
                    "오늘의 퀴즈가 이미 존재하여 생성을 건너뜁니다.");
        }

        List<Stock> activeStocks = stockRepository.findByIsActiveTrueOrderByStockCodeAsc();
        if (activeStocks.isEmpty()) {
            return new ScheduleTriggerResponseDto(
                    "create-quiz",
                    false,
                    0,
                    "활성화된 종목이 없어 퀴즈를 생성할 수 없습니다.");
        }

        Stock selectedStock = activeStocks.get(0);
        BigDecimal currentPrice = stockScraper.getStockPrice(selectedStock.getStockCode());

        StockQuizDaily todayQuiz = StockQuizDaily.builder()
                .quizDate(today)
                .stock(selectedStock)
                .base_price(currentPrice)
                .status("OPEN")
                .build();

        StockQuizDaily savedQuiz = quizRepository.save(todayQuiz);
        return new ScheduleTriggerResponseDto(
                "create-quiz",
                true,
                1,
                "일일 퀴즈 생성 완료: quizId=" + savedQuiz.getQuizId()
                        + ", stock=" + selectedStock.getStockName()
                        + "(" + selectedStock.getStockCode() + ")"
                        + ", basePrice=" + currentPrice);
    }

    @Transactional
    public ScheduleTriggerResponseDto triggerCloseTodayQuiz() {
        LocalDate today = currentDate();

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return new ScheduleTriggerResponseDto(
                    "close-quiz",
                    false,
                    0,
                    "오늘(" + today + ")은 주말이라 퀴즈 종료를 건너뜁니다.");
        }

        if (holidayRepository.existsByHolidayDate(today)) {
            return new ScheduleTriggerResponseDto(
                    "close-quiz",
                    false,
                    0,
                    "오늘(" + today + ")은 휴장일이라 퀴즈 종료를 건너뜁니다.");
        }

        StockQuizDaily todayQuiz = quizRepository.findByQuizDate(today).orElse(null);

        if (todayQuiz == null) {
            return new ScheduleTriggerResponseDto(
                    "close-quiz",
                    false,
                    0,
                    "오늘의 퀴즈가 존재하지 않아 종료를 건너뜁니다.");
        }

        if (!"OPEN".equals(todayQuiz.getStatus())) {
            return new ScheduleTriggerResponseDto(
                    "close-quiz",
                    false,
                    0,
                    "오늘의 퀴즈가 이미 OPEN 상태가 아닙니다. 현재 상태: " + todayQuiz.getStatus());
        }

        todayQuiz.setStatus("CLOSED");
        quizRepository.save(todayQuiz);

        return new ScheduleTriggerResponseDto(
                "close-quiz",
                true,
                1,
                "퀴즈 투표 종료: quizId=" + todayQuiz.getQuizId() + ", date=" + today);
    }

    LocalDate currentDate() {
        return LocalDate.now();
    }
}
