package com.stockgame.controller;

import com.stockgame.dto.ScheduleTriggerResponseDto;
import com.stockgame.service.QuizScheduleService;
import com.stockgame.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalScheduleControllerUnitTest {

    @Mock
    private QuizScheduleService quizScheduleService;

    @Mock
    private SettlementService settlementService;

    private LocalScheduleController controller;

    @BeforeEach
    void setUp() {
        controller = new LocalScheduleController(quizScheduleService, settlementService);
    }

    @Test
    void createQuiz_returnsServiceResult() {
        ScheduleTriggerResponseDto expected =
                new ScheduleTriggerResponseDto("create-quiz", true, 1, "created");
        when(quizScheduleService.triggerCreateDailyQuiz()).thenReturn(expected);

        ResponseEntity<ScheduleTriggerResponseDto> response = controller.createQuiz();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void closeQuiz_returnsServiceResult() {
        ScheduleTriggerResponseDto expected =
                new ScheduleTriggerResponseDto("close-quiz", true, 1, "closed");
        when(quizScheduleService.triggerCloseTodayQuiz()).thenReturn(expected);

        ResponseEntity<ScheduleTriggerResponseDto> response = controller.closeQuiz();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void settle_returnsServiceResult() {
        ScheduleTriggerResponseDto expected =
                new ScheduleTriggerResponseDto("settle", false, 0, "nothing to settle");
        when(settlementService.triggerSettlement()).thenReturn(expected);

        ResponseEntity<ScheduleTriggerResponseDto> response = controller.settle();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void settleToday_returnsServiceResult() {
        ScheduleTriggerResponseDto expected =
                new ScheduleTriggerResponseDto("settle-today", true, 1, "settled today");
        when(settlementService.triggerSettleToday()).thenReturn(expected);

        ResponseEntity<ScheduleTriggerResponseDto> response = controller.settleToday();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }
}
