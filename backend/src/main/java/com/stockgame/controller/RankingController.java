package com.stockgame.controller;

import com.stockgame.dto.MyRankingDto;
import com.stockgame.dto.RankingEntryDto;
import com.stockgame.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/top")
    public ResponseEntity<List<RankingEntryDto>> getTopRankings(
            @RequestParam(defaultValue = "5") int limit) {
        limit = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(rankingService.getTopRankings(limit));
    }

    @GetMapping("/me")
    public ResponseEntity<MyRankingDto> getMyRanking(
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(rankingService.getMyRanking(principal));
    }
}
