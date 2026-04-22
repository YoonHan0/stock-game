package com.stockgame.dto;

public record ScheduleTriggerResponseDto(
        String action,
        boolean executed,
        int affectedCount,
        String message
) {
}
