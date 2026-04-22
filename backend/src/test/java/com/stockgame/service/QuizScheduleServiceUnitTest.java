package com.stockgame.service;

import com.stockgame.domain.entity.Stock;
import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.StockRepository;
import com.stockgame.dto.ScheduleTriggerResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizScheduleServiceUnitTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockQuizRepository quizRepository;

    @Mock
    private StockScraper stockScraper;

    @Mock
    private MarketHolidayRepository holidayRepository;

    private QuizScheduleService service;

    @BeforeEach
    void setUp() {
        service = spy(new QuizScheduleService(stockRepository, quizRepository, stockScraper, holidayRepository));
    }

    @Test
    void triggerCloseTodayQuiz_weekend_skips() {
        LocalDate saturday = LocalDate.of(2025, 5, 31);
        doReturn(saturday).when(service).currentDate();

        ScheduleTriggerResponseDto result = service.triggerCloseTodayQuiz();

        assertThat(result.executed()).isFalse();
        assertThat(result.action()).isEqualTo("close-quiz");
        assertThat(result.message()).contains("주말");
        verifyNoInteractions(holidayRepository, quizRepository);
    }

    @Test
    void triggerCloseTodayQuiz_noQuizForToday_skips() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByQuizDate(today)).thenReturn(Optional.empty());

        ScheduleTriggerResponseDto result = service.triggerCloseTodayQuiz();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("존재하지 않아");
    }

    @Test
    void triggerCloseTodayQuiz_alreadyClosed_skips() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        StockQuizDaily closedQuiz = StockQuizDaily.builder()
                .quizId(1L)
                .quizDate(today)
                .status("CLOSED")
                .build();

        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByQuizDate(today)).thenReturn(Optional.of(closedQuiz));

        ScheduleTriggerResponseDto result = service.triggerCloseTodayQuiz();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("이미 OPEN 상태가 아닙니다");
        verify(quizRepository, never()).save(any());
    }

    @Test
    void triggerCloseTodayQuiz_openQuiz_closesSuccessfully() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        Stock stock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .isActive(true)
                .build();

        StockQuizDaily openQuiz = StockQuizDaily.builder()
                .quizId(1L)
                .quizDate(today)
                .stock(stock)
                .base_price(new BigDecimal("70000"))
                .status("OPEN")
                .build();

        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByQuizDate(today)).thenReturn(Optional.of(openQuiz));
        when(quizRepository.save(any())).thenReturn(openQuiz);

        ScheduleTriggerResponseDto result = service.triggerCloseTodayQuiz();

        assertThat(result.executed()).isTrue();
        assertThat(result.affectedCount()).isEqualTo(1);
        assertThat(result.message()).contains("퀴즈 투표 종료");
        
        verify(quizRepository).save(argThat(quiz -> 
            "CLOSED".equals(quiz.getStatus())
        ));
    }

    @Test
    void triggerCloseTodayQuiz_holiday_skips() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        when(holidayRepository.existsByHolidayDate(today)).thenReturn(true);

        ScheduleTriggerResponseDto result = service.triggerCloseTodayQuiz();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("휴장일");
        verify(quizRepository, never()).save(any());
    }
}
