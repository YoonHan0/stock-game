package com.stockgame.service;

import com.stockgame.domain.entity.Stock;
import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.domain.repository.VoteRepository;
import com.stockgame.dto.ScheduleTriggerResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceUnitTest {

    @Mock
    private StockQuizRepository quizRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockScraper stockScraper;

    @Mock
    private MarketHolidayRepository holidayRepository;

    private SettlementService service;

    @BeforeEach
    void setUp() {
        service = spy(new SettlementService(quizRepository, voteRepository, userRepository, stockScraper, holidayRepository));
    }

    @Test
    void triggerSettlement_noQuizzes_skips() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByStatusInAndQuizDateBefore(List.of("OPEN", "CLOSED"), today))
                .thenReturn(Collections.emptyList());

        ScheduleTriggerResponseDto result = service.triggerSettlement();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("정산할 퀴즈가 없습니다");
    }

    @Test
    void triggerSettlement_closedQuiz_isStillSettledOnNextTradingDay() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        LocalDate yesterday = today.minusDays(1);

        Stock stock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .build();

        StockQuizDaily closedQuiz = StockQuizDaily.builder()
                .quizId(1L)
                .quizDate(yesterday)
                .stock(stock)
                .base_price(new BigDecimal("70000"))
                .status("CLOSED")
                .build();

        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByStatusInAndQuizDateBefore(List.of("OPEN", "CLOSED"), today))
                .thenReturn(Collections.singletonList(closedQuiz));
        when(voteRepository.findByQuizQuizId(1L)).thenReturn(Collections.emptyList());
        when(stockScraper.getStockPrice("005930")).thenReturn(new BigDecimal("71000"));

        ScheduleTriggerResponseDto result = service.triggerSettlement();

        assertThat(result.executed()).isTrue();
        assertThat(result.affectedCount()).isEqualTo(1);
        assertThat(closedQuiz.getStatus()).isEqualTo("SETTLED");
        assertThat(closedQuiz.getQuizResult()).isEqualTo("UP");
    }

    @Test
    void triggerSettlement_openAndClosedQuizzes_settlesBoth() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        Stock stock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .build();

        StockQuizDaily openQuiz = StockQuizDaily.builder()
                .quizId(1L)
                .quizDate(twoDaysAgo)
                .stock(stock)
                .base_price(new BigDecimal("70000"))
                .status("OPEN")
                .build();

        StockQuizDaily closedQuiz = StockQuizDaily.builder()
                .quizId(2L)
                .quizDate(yesterday)
                .stock(stock)
                .base_price(new BigDecimal("71000"))
                .status("CLOSED")
                .build();

        when(holidayRepository.existsByHolidayDate(today)).thenReturn(false);
        when(quizRepository.findByStatusInAndQuizDateBefore(List.of("OPEN", "CLOSED"), today))
                .thenReturn(List.of(openQuiz, closedQuiz));
        when(voteRepository.findByQuizQuizId(anyLong())).thenReturn(Collections.emptyList());
        when(stockScraper.getStockPrice("005930")).thenReturn(new BigDecimal("72000"));

        ScheduleTriggerResponseDto result = service.triggerSettlement();

        assertThat(result.executed()).isTrue();
        assertThat(result.affectedCount()).isEqualTo(2);
        assertThat(openQuiz.getStatus()).isEqualTo("SETTLED");
        assertThat(closedQuiz.getStatus()).isEqualTo("SETTLED");
    }

    @Test
    void triggerSettlement_weekend_skips() {
        LocalDate saturday = LocalDate.of(2025, 5, 31);
        doReturn(saturday).when(service).currentDate();

        ScheduleTriggerResponseDto result = service.triggerSettlement();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("주말");
        verifyNoInteractions(holidayRepository, quizRepository);
    }

    @Test
    void triggerSettlement_holiday_skips() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        when(holidayRepository.existsByHolidayDate(today)).thenReturn(true);

        ScheduleTriggerResponseDto result = service.triggerSettlement();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("휴장일");
        verify(quizRepository, never()).findByStatusInAndQuizDateBefore(any(), any());
    }

    @Test
    void triggerSettleToday_openQuiz_settlesToday() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        Stock stock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .build();

        StockQuizDaily openQuiz = StockQuizDaily.builder()
                .quizId(3L)
                .quizDate(today)
                .stock(stock)
                .base_price(new BigDecimal("70000"))
                .status("OPEN")
                .build();

        when(quizRepository.findByQuizDate(today)).thenReturn(java.util.Optional.of(openQuiz));
        when(voteRepository.findByQuizQuizId(3L)).thenReturn(Collections.emptyList());
        when(stockScraper.getStockPrice("005930")).thenReturn(new BigDecimal("70500"));

        ScheduleTriggerResponseDto result = service.triggerSettleToday();

        assertThat(result.executed()).isTrue();
        assertThat(result.affectedCount()).isEqualTo(1);
        assertThat(result.message()).contains("previousStatus=OPEN", "currentStatus=SETTLED");
        assertThat(openQuiz.getStatus()).isEqualTo("SETTLED");
        assertThat(openQuiz.getQuizResult()).isEqualTo("UP");
    }

    @Test
    void triggerSettleToday_closedQuiz_settlesToday() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        Stock stock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .build();

        StockQuizDaily closedQuiz = StockQuizDaily.builder()
                .quizId(4L)
                .quizDate(today)
                .stock(stock)
                .base_price(new BigDecimal("70000"))
                .status("CLOSED")
                .build();

        when(quizRepository.findByQuizDate(today)).thenReturn(java.util.Optional.of(closedQuiz));
        when(voteRepository.findByQuizQuizId(4L)).thenReturn(Collections.emptyList());
        when(stockScraper.getStockPrice("005930")).thenReturn(new BigDecimal("69500"));

        ScheduleTriggerResponseDto result = service.triggerSettleToday();

        assertThat(result.executed()).isTrue();
        assertThat(result.message()).contains("previousStatus=CLOSED", "currentStatus=SETTLED");
        assertThat(closedQuiz.getStatus()).isEqualTo("SETTLED");
        assertThat(closedQuiz.getQuizResult()).isEqualTo("DOWN");
    }

    @Test
    void triggerSettleToday_noQuiz_returnsClearMessage() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();
        when(quizRepository.findByQuizDate(today)).thenReturn(java.util.Optional.empty());

        ScheduleTriggerResponseDto result = service.triggerSettleToday();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("오늘(" + today + ") 퀴즈가 없어");
        verifyNoInteractions(voteRepository, stockScraper, userRepository);
    }

    @Test
    void triggerSettleToday_alreadySettled_skipsWithCurrentState() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        StockQuizDaily quiz = StockQuizDaily.builder()
                .quizId(5L)
                .quizDate(today)
                .status("SETTLED")
                .build();

        when(quizRepository.findByQuizDate(today)).thenReturn(java.util.Optional.of(quiz));

        ScheduleTriggerResponseDto result = service.triggerSettleToday();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("현재 상태가 SETTLED", "정산 가능 상태=OPEN, CLOSED");
        verifyNoInteractions(voteRepository, stockScraper, userRepository);
    }

    @Test
    void triggerSettleToday_nonSettleableState_skipsWithCurrentState() {
        LocalDate today = LocalDate.of(2025, 6, 2);
        doReturn(today).when(service).currentDate();

        StockQuizDaily quiz = StockQuizDaily.builder()
                .quizId(6L)
                .quizDate(today)
                .status("CREATED")
                .build();

        when(quizRepository.findByQuizDate(today)).thenReturn(java.util.Optional.of(quiz));

        ScheduleTriggerResponseDto result = service.triggerSettleToday();

        assertThat(result.executed()).isFalse();
        assertThat(result.message()).contains("현재 상태가 CREATED", "정산 가능 상태=OPEN, CLOSED");
        verifyNoInteractions(voteRepository, stockScraper, userRepository);
    }
}
