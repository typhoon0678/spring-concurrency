package dev.typhoon.spring_concurrency.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 낙관적 락 구현 위한 version 필드 추가
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long quantity;

    @Version
    private Long version;

    @Builder
    public TicketVersion(String name, Long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.name = name;
        this.quantity = quantity;
    }

    public void updateQuantity(Long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.quantity = quantity;
    }

    public void reduceQuantity() {
        if (quantity <= 0) {
            throw new IllegalStateException("Quantity cannot be reduced below zero");
        }

        this.quantity--;
    }
}
