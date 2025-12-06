package com.monkcommerce.monk_commerce_backend_task.service.strategy;

import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import com.monkcommerce.monk_commerce_backend_task.entity.enums.CouponType;
import org.springframework.stereotype.Component;

@Component
public class CouponStrategyFactory {
    private final CartWiseCouponStrategy cartWise;
    private final ProductWiseCouponStrategy productWise;
    private final BxGyCouponStrategy bxgy;

    public CouponStrategyFactory(CartWiseCouponStrategy cartWise,
                                 ProductWiseCouponStrategy productWise,
                                 BxGyCouponStrategy bxgy) {
        this.cartWise = cartWise;
        this.productWise = productWise;
        this.bxgy = bxgy;
    }

    public CouponStrategy getStrategy(Coupon coupon) {
        if (coupon.getType() == CouponType.CART_WISE) return cartWise;
        if (coupon.getType() == CouponType.PRODUCT_WISE) return productWise;
        if (coupon.getType() == CouponType.BXGY) return bxgy;
        throw new IllegalArgumentException("Unknown coupon type: " + coupon.getType());
    }
}
