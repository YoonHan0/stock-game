package com.stockgame.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class UserProfileDto {
    private String nickname;
    private String email;
    private Long points;
    private long totalVotes;
    private long correctVotes;
    private double accuracy;
}
