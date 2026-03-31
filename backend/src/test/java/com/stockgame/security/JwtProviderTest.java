package com.stockgame.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(jwtProvider, "cookieSecure", false);
        ReflectionTestUtils.setField(jwtProvider, "sameSite", "Lax");
        ReflectionTestUtils.invokeMethod(jwtProvider, "init");
    }

    @Test
    void token_roundTrip_success() {
        String token = jwtProvider.createToken(10L, "u@example.com", "ROLE_USER");

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(10L);
        assertThat(jwtProvider.getEmail(token)).isEqualTo("u@example.com");
        assertThat(jwtProvider.getRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    void validateToken_false_whenMalformed() {
        assertThat(jwtProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void createAccessTokenCookie_containsExpectedOptions() {
        ResponseCookie cookie = jwtProvider.createAccessTokenCookie("jwt-token");

        assertThat(cookie.getName()).isEqualTo("accessToken");
        assertThat(cookie.getValue()).isEqualTo("jwt-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.toString()).contains("SameSite=Lax");
        assertThat(cookie.getMaxAge().toSeconds()).isEqualTo(3600);
    }

    @Test
    void createExpiredAccessTokenCookie_setsZeroMaxAge() {
        ResponseCookie cookie = jwtProvider.createExpiredAccessTokenCookie();

        assertThat(cookie.getName()).isEqualTo("accessToken");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().isZero()).isTrue();
    }
}
