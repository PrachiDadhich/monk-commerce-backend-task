package com.monkcommerce.monk_commerce_backend_task.service.strategy;

import com.monkcommerce.monk_commerce_backend_task.dto.ApplicableCouponResponse;
import com.monkcommerce.monk_commerce_backend_task.dto.CartRequest;
import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;

public interface CouponStrategy {
    boolean isApplicable(CartRequest cartRequest, Coupon coupon);
    ApplicableCouponResponse computeDiscount(CartRequest cartRequest, Coupon coupon);
    CartRequest apply(CartRequest cartRequest, Coupon coupon);
}
