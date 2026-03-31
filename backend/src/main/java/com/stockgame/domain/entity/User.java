package com.stockgame.domain.entity;

import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 계정 정보 ──────────────────────────────────────────────────────────────

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    /** 일반(LOCAL) 가입용 비밀번호 해시. 소셜 로그인 시 null 허용 */
    @Column(name = "password")
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    // ── 소셜 로그인 ───────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider;

    /** 소셜 제공자가 발급한 고유 ID. LOCAL 가입 시 null 허용 */
    @Column(name = "provider_id")
    private String providerId;

    // ── 권한 ──────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    // ── 포인트 ────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    @Builder.Default
    private Long points = 0L;

    // ── 감사 시각 ─────────────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── 비즈니스 메서드 ───────────────────────────────────────────────────────

    /**
     * 포인트 지급.
     *
     * @param amount 지급할 포인트 (양수 필수)
     * @throws IllegalArgumentException amount 가 0 이하인 경우
     */
    public void addPoints(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("지급 포인트는 0보다 커야 합니다. amount=" + amount);
        }
        this.points += amount;
    }

    /**
     * 일반(LOCAL) 가입 사용자 생성.
     */
    public static User ofLocal(String email, String passwordHash, String nickname) {
        return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .provider(AuthProvider.LOCAL)
                .providerId("local_:" + UUID.randomUUID())
                .role(UserRole.USER)
                .points(0L)
                .build();
    }

    /**
     * 소셜 로그인으로 신규 사용자를 생성하는 정적 팩토리 메서드.
     */
    public static User ofSocial(String email, String nickname,
                                AuthProvider provider, String providerId) {
        return User.builder()
                .email(email)
                .passwordHash(null)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .points(0L)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateSocialInfo(AuthProvider provider, String providerId, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
    }
}
