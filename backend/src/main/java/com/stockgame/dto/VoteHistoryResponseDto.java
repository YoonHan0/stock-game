package com.stockgame.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class VoteHistoryResponseDto {
    private List<VoteHistoryItemDto> items;
    private int page;
    private int totalPages;
    private long totalItems;
}
