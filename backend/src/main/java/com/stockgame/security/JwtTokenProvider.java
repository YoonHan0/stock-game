package com.stockgame.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        // 이 부분이 핵심입니다. 256비트 미만이면 여기서 명확한 예외를 던지도록 설계
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("email", String.class);
    }
}
