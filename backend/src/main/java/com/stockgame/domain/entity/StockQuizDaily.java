package com.stockgame.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_quiz_daily")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StockQuizDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @Column(nullable = false)
    private BigDecimal base_price;

    private BigDecimal final_price;

    @Builder.Default
    private String status = "OPEN";

    @Column(name = "quiz_result")
    private String quizResult;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    private LocalDateTime created_at;

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.final_price = finalPrice;
    }

    public void setQuizResult(String quizResult) {
        this.quizResult = quizResult;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    @Override
    public String toString() {
        return "StockQuizDaily{" +
                "quizId=" + quizId +
                ", quizDate=" + quizDate +
                ", stock=" + stock +
                ", base_price=" + base_price +
                ", final_price=" + final_price +
                ", status='" + status + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}