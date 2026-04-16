package com.stockgame.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MyRankingDto {
    private long rank;
    private long totalUsers;
    private Long points;
}
