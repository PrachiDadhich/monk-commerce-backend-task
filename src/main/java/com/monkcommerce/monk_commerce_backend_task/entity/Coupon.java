package com.monkcommerce.monk_commerce_backend_task.entity;

import com.monkcommerce.monk_commerce_backend_task.entity.enums.CouponType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Lob
    @Column(nullable = false)
    private String detailsJson;

    private LocalDate expiresAt;

    private String name;

    public Coupon(CouponType type, String detailsJson, LocalDate expiresAt, String name) {
        this.type = type;
        this.detailsJson = detailsJson;
        this.expiresAt = expiresAt;
        this.name = name;
    }

    public void updateDetails(CouponType type, String detailsJson, LocalDate expiresAt, String name) {
        this.type = type;
        this.detailsJson = detailsJson;
        this.expiresAt = expiresAt;
        this.name = name;
    }
}
