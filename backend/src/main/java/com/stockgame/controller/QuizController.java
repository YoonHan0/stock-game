package com.stockgame.controller;

import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.dto.ApiErrorResponseDto;
import com.stockgame.dto.QuizResponseDto;
import com.stockgame.dto.VoteRequestDto;
import com.stockgame.dto.VoteStatsDto;
import com.stockgame.service.QuizService;
import com.stockgame.service.QuizVoteConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/today")
    public ResponseEntity<QuizResponseDto> getTodayQuiz() {

        System.out.println("=== 오늘의 퀴즈 호출 ===");

        // 1. 오늘 날짜의 퀴즈 정보 조회
        StockQuizDaily quiz = quizService.getTodayQuiz();

        System.out.println("=== 퀴즈 확인 ===");
        System.out.println(quiz.toString());

        // 2. 실시간 가격 정보 스크래핑
        BigDecimal currentLivePrice = quizService.getCurrentLivePrice(quiz.getStock().getStockCode());

        System.out.println("=== 현재가 확인 ===");
        System.out.println(currentLivePrice);

        // 3. DTO 변환 및 반환
        QuizResponseDto response = QuizResponseDto.builder()
                .quizId(quiz.getQuizId())
                .stockName(quiz.getStock().getStockName())
                .stockCode(quiz.getStock().getStockCode())
                .basePrice(quiz.getBase_price())
                .currentPrice(currentLivePrice)
                .isMarketClosed(quizService.isMarketClosed())
                .build();

        System.out.println("=== 반환값 확인 ===");
        System.out.println(response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/vote")
    public ResponseEntity<String> vote(@RequestBody VoteRequestDto dto,
                                       @AuthenticationPrincipal Object principal) {
        quizService.saveVote(dto, principal);   // userId 결정 + 검증은 서비스에서 처리
        return ResponseEntity.ok("투표가 성공적으로 기록되었습니다!");
    }

    @GetMapping("/{quizId}/stats")
    public ResponseEntity<VoteStatsDto> getStats(@PathVariable("quizId") Long quizId) {
        return ResponseEntity.ok(quizService.getVoteStats(quizId));
    }

    @GetMapping("/{quizId}/check-vote")
    public ResponseEntity<Boolean> getIsVoteState(
            @PathVariable("quizId") Long quizId,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(quizService.getIsVoteState(quizId, principal)); // 서비스에서 처리
    }

    @ExceptionHandler(QuizVoteConflictException.class)
    public ResponseEntity<ApiErrorResponseDto> handleQuizVoteConflict(QuizVoteConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponseDto(e.getCode(), e.getMessage()));
    }
}
