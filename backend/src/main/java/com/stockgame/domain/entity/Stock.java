package com.stockgame.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock {
    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "is_active")
    private boolean isActive;

    @Override
    public String toString() {
        return "Stock{" +
                "stockCode='" + stockCode + '\'' +
                ", stockName='" + stockName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
