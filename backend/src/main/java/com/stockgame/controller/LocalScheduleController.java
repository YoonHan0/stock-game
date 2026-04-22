package com.stockgame.controller;

import com.stockgame.dto.ScheduleTriggerResponseDto;
import com.stockgame.service.QuizScheduleService;
import com.stockgame.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/api/local/schedules")
@RequiredArgsConstructor
public class LocalScheduleController {

    private final QuizScheduleService quizScheduleService;
    private final SettlementService settlementService;

    @PostMapping("/create-quiz")
    public ResponseEntity<ScheduleTriggerResponseDto> createQuiz() {
        return ResponseEntity.ok(quizScheduleService.triggerCreateDailyQuiz());
    }

    @PostMapping("/close-quiz")
    public ResponseEntity<ScheduleTriggerResponseDto> closeQuiz() {
        return ResponseEntity.ok(quizScheduleService.triggerCloseTodayQuiz());
    }

    @PostMapping("/settle")
    public ResponseEntity<ScheduleTriggerResponseDto> settle() {
        return ResponseEntity.ok(settlementService.triggerSettlement());
    }

    @PostMapping("/settle-today")
    public ResponseEntity<ScheduleTriggerResponseDto> settleToday() {
        return ResponseEntity.ok(settlementService.triggerSettleToday());
    }
}
