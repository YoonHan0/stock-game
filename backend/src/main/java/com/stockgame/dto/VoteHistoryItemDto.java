package com.stockgame.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class VoteHistoryItemDto {
    private Long quizId;
    private LocalDate quizDate;
    private String stockName;
    private String stockCode;
    private String prediction;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private String quizResult;
    private String settlementStatus;
}
