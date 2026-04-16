package com.stockgame.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class RankingEntryDto {
    private long rank;
    private String nickname;
    private Long points;
}
