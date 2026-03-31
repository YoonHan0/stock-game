package com.stockgame.service;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.enums.UserRole;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.LoginRequestDto;
import com.stockgame.dto.SignupRequestDto;
import com.stockgame.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void signup_success_normalizesEmailAndCreatesHashedLocalUser() {
        SignupRequestDto request = new SignupRequestDto("  TEST@EXAMPLE.COM  ", "plainPw", null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPw")).thenReturn("hashedPw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = authService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(created.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashedPw");
        assertThat(saved.getNickname()).isEqualTo("test");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void signup_fail_whenEmailAlreadyExists() {
        SignupRequestDto request = new SignupRequestDto("dup@example.com", "pw", "nick");
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(User.builder().build()));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AuthBadRequestException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    void login_success_returnsJwtCookieAndUser() {
        LoginRequestDto request = new LoginRequestDto("user@example.com", "plainPw");
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hashedPw")
                .nickname("nick")
                .provider(AuthProvider.LOCAL)
                .role(UserRole.USER)
                .points(0L)
                .build();

        ResponseCookie cookie = ResponseCookie.from("accessToken", "jwt-token").httpOnly(true).path("/").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPw", "hashedPw")).thenReturn(true);
        when(jwtProvider.createToken(1L, "user@example.com", "ROLE_USER")).thenReturn("jwt-token");
        when(jwtProvider.createAccessTokenCookie("jwt-token")).thenReturn(cookie);

        AuthService.LoginResult result = authService.login(request);

        assertThat(result.user().getEmail()).isEqualTo("user@example.com");
        assertThat(result.cookie().getName()).isEqualTo("accessToken");
        assertThat(result.cookie().isHttpOnly()).isTrue();
    }

    @Test
    void login_fail_whenSocialOnlyAccount() {
        LoginRequestDto request = new LoginRequestDto("social@example.com", "plainPw");
        User socialUser = User.builder()
                .id(2L)
                .email("social@example.com")
                .passwordHash(null)
                .nickname("social")
                .provider(AuthProvider.GOOGLE)
                .role(UserRole.USER)
                .points(0L)
                .build();

        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(socialUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessageContaining("소셜 로그인 전용");
    }

    @Test
    void logoutCookie_returnsExpiredCookie() {
        ResponseCookie expired = ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .build();
        when(jwtProvider.createExpiredAccessTokenCookie()).thenReturn(expired);

        ResponseCookie result = authService.logoutCookie();

        assertThat(result.getName()).isEqualTo("accessToken");
        assertThat(result.getMaxAge().isZero()).isTrue();
    }
}
