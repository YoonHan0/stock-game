package com.stockgame.controller;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.ApiErrorResponseDto;
import com.stockgame.dto.AuthMeResponseDto;
import com.stockgame.dto.LoginRequestDto;
import com.stockgame.dto.SignupRequestDto;
import com.stockgame.service.AuthBadRequestException;
import com.stockgame.service.AuthService;
import com.stockgame.service.AuthUnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthMeResponseDto> signup(@RequestBody SignupRequestDto request) {
        User created = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthMeResponseDto.from(created));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthMeResponseDto> login(@RequestBody LoginRequestDto request) {
        AuthService.LoginResult result = authService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(AuthMeResponseDto.from(result.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.logoutCookie().toString())
                .body(Map.of("message", "로그아웃 되었습니다."));
    }

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

    @ExceptionHandler(AuthBadRequestException.class)
    public ResponseEntity<ApiErrorResponseDto> handleBadRequest(AuthBadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(AuthUnauthorizedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnauthorized(AuthUnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponseDto(e.getMessage()));
    }

    private Optional<User> resolveUser(Object principal) {
        if (principal instanceof Long userId) {
            return userRepository.findById(userId);
        }

        if (principal instanceof String principalStr) {
            if (!StringUtils.hasText(principalStr)) {
                return Optional.empty();
            }

            Optional<User> byEmail = userRepository.findByEmail(principalStr);
            if (byEmail.isPresent()) {
                return byEmail;
            }

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
