package com.monkcommerce.monk_commerce_backend_task.dto;

import com.monkcommerce.monk_commerce_backend_task.entity.enums.CouponType;

public record ApplicableCouponResponse(
        Long couponId,
        String type,
        double discount
) {
    public static ApplicableCouponResponse fromCalculations(
            Long couponId,
            String type,
            double discount
    ) {
        return new ApplicableCouponResponse(couponId, type, discount);
    }
}