package com.stockgame.service;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.LoginRequestDto;
import com.stockgame.dto.SignupRequestDto;
import com.stockgame.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public User signup(SignupRequestDto request) {
        String email = normalizeEmail(request.email());
        String rawPassword = request.password();

        if (!StringUtils.hasText(email)) {
            throw new AuthBadRequestException("email은 필수입니다.");
        }
        if (!StringUtils.hasText(rawPassword)) {
            throw new AuthBadRequestException("password는 필수입니다.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthBadRequestException("이미 사용 중인 이메일입니다.");
        }

        String nickname = buildNickname(email, request.nickname());
        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = User.ofLocal(email, passwordHash, nickname);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequestDto request) {
        String email = normalizeEmail(request.email());
        String rawPassword = request.password();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            throw new AuthBadRequestException("email/password를 모두 입력해 주세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthUnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new AuthUnauthorizedException("소셜 로그인 전용 계정입니다. 소셜 로그인을 이용해 주세요.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthUnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String role = "ROLE_" + user.getRole().name();
        String token = jwtProvider.createToken(user.getId(), user.getEmail(), role);
        ResponseCookie cookie = jwtProvider.createAccessTokenCookie(token);

        return new LoginResult(user, cookie);
    }

    public ResponseCookie logoutCookie() {
        return jwtProvider.createExpiredAccessTokenCookie();
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        return email.trim().toLowerCase();
    }

    private String buildNickname(String email, String nickname) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "user";
        }
        return email.substring(0, atIndex);
    }

    public record LoginResult(User user, ResponseCookie cookie) {
    }
}
