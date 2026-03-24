package com.stockgame.controller;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.AuthMeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponseDto> me(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = resolveUser(principal);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(AuthMeResponseDto.from(userOpt.get()));
    }

    private Optional<User> resolveUser(Object principal) {
        if (principal instanceof Long userId) {
            return userRepository.findById(userId);
        }

        if (principal instanceof String principalStr) {
            if (!StringUtils.hasText(principalStr)) {
                return Optional.empty();
            }

            // 현재 JWT 필터는 email을 principal로 저장하므로 email 조회를 우선합니다.
            Optional<User> byEmail = userRepository.findByEmail(principalStr);
            if (byEmail.isPresent()) {
                return byEmail;
            }

            // 하위 호환: principal이 숫자 문자열인 경우 userId 조회 시도
            try {
                Long userId = Long.parseLong(principalStr);
                return userRepository.findById(userId);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}

