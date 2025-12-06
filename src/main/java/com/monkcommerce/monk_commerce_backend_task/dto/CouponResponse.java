package com.monkcommerce.monk_commerce_backend_task.dto;

import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import com.monkcommerce.monk_commerce_backend_task.entity.enums.CouponType;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

public record CouponResponse(
        Long id,
        CouponType type,
        String detailsJson,
        LocalDate expiresAt,
        String name
) {
    public static CouponResponse fromEntity(Coupon entity) {
        return new CouponResponse(
                entity.getId(),
                entity.getType(),
                entity.getDetailsJson(),
                entity.getExpiresAt(),
                entity.getName()
        );
    }
}