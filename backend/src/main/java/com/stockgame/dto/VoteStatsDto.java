package com.stockgame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class VoteStatsDto {

    private long upCount;
    private long downCount;
    private double upPercentage;
    private double downPercentage;
}
