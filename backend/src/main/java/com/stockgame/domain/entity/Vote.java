package com.stockgame.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vote_id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // Supabase Auth의 UUID와 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private StockQuizDaily quiz;

    @Column(nullable = false)
    private String prediction; // "UP" or "DOWN"

    private Boolean is_correct;

    private LocalDateTime created_at;

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
    }
}