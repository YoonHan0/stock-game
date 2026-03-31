package com.stockgame.dto;

public record SignupRequestDto(
        String email,
        String password,
        String nickname
) {
}

