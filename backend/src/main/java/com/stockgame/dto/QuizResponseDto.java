package com.stockgame.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class QuizResponseDto {

    private Long quizId;
    private String stockName;
    private String stockCode;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private boolean isMarketClosed;
    private LocalDate quizDate;
    private String quizResult;
    private String status;

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