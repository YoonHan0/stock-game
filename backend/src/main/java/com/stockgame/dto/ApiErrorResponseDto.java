package com.stockgame.dto;

public record ApiErrorResponseDto(String code, String message) {

    // 기존 new ApiErrorResponseDto(message) 호출과의 하위 호환
    public ApiErrorResponseDto(String message) {
        this(null, message);
    }
}
