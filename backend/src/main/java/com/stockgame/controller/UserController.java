package com.stockgame.controller;

import com.stockgame.dto.UserProfileDto;
import com.stockgame.dto.VoteHistoryResponseDto;
import com.stockgame.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(userService.getUserProfile(principal));
    }

    @GetMapping("/vote-history")
    public ResponseEntity<VoteHistoryResponseDto> getVoteHistory(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        return ResponseEntity.ok(userService.getVoteHistory(principal, page, size));
    }
}
