package com.stockgame.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class QuizResponseDto {

    private Long quizId;
    private String stockName;
    private String stockCode;
    private BigDecimal basePrice;       // 퀴즈 생성 시점 기준가
    private BigDecimal currentPrice;    // 현재 실시간 가격
    private boolean isMarketClosed;     // 오후 4시 이후인지 여부

    @Override
    public String toString() {
        return "QuizResponseDto{" +
                "quizId=" + quizId +
                ", stockName='" + stockName + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", basePrice=" + basePrice +
                ", currentPrice=" + currentPrice +
                ", isMarketClosed=" + isMarketClosed +
                '}';
    }
}