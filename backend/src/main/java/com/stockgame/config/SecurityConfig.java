package com.stockgame.config;

import com.stockgame.security.JwtAuthenticationFilter;
import com.stockgame.security.OAuth2SuccessHandler;
import com.stockgame.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter            jwtAuthenticationFilter;
    private final OAuth2SuccessHandler               oAuth2SuccessHandler;
    private final CustomOAuth2UserService            customOAuth2UserService;

    /**
     * 인증 없이 접근 가능한 경로
     */
    private static final String[] PERMIT_ALL_PATTERNS = {
            "/api/auth/**",
            "/login/**",
            "/oauth2/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF 비활성화 (REST API + JWT 쿠키 방식) ──────────────────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── 세션 STATELESS (JWT 기반이므로 서버 세션 미사용) ─────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── CORS ─────────────────────────────────────────────────────────────
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── 경로별 접근 권한 ─────────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS preflight 는 인증 없이 항상 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PERMIT_ALL_PATTERNS).permitAll()
                        .anyRequest().authenticated()
                )

                // ── OAuth2 소셜 로그인 ────────────────────────────────────────────
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // ── JWT 쿠키 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입 ───
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // addAllowedOrigin 대신 setAllowedOrigins 사용 → 기존 목록을 덮어써서 origin 중복 방지
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

