package com.stockgame.domain.entity;

import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    /** 일반(LOCAL) 가입용. 소셜 로그인 시 null 허용 */
    @Column
    private String password;

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
        this.updatedAt  = LocalDateTime.now();
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
     * 소셜 로그인으로 신규 사용자를 생성하는 정적 팩토리 메서드.
     *
     * @param email      소셜 제공자로부터 받은 이메일
     * @param nickname   초기 닉네임 (소셜 프로필 이름 등)
     * @param provider   소셜 제공자 (GOOGLE, KAKAO 등)
     * @param providerId 소셜 제공자가 발급한 고유 식별자
     * @return 저장 전 새 User 인스턴스
     */
    public static User ofSocial(String email, String nickname,
                                AuthProvider provider, String providerId) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .points(0L)
                .build();
    }

    /**
     * 소셜 재로그인 시 닉네임을 최신 소셜 프로필로 동기화합니다.
     *
     * @param nickname 소셜 제공자로부터 받은 최신 닉네임
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 소셜 로그인 재인증 시 provider/providerId/nickname 을 최신 정보로 동기화합니다.
     */
    public void updateSocialInfo(AuthProvider provider, String providerId, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
    }
}

