package com.stockgame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequestDto {

    private Long quizId;
    private String prediction; // "UP" 또는 "DOWN"
    private Long userId;
}
