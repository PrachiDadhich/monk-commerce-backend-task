package com.monkcommerce.monk_commerce_backend_task.service;

import com.monkcommerce.monk_commerce_backend_task.dto.ApplicableCouponResponse;
import com.monkcommerce.monk_commerce_backend_task.dto.CartRequest;
import com.monkcommerce.monk_commerce_backend_task.dto.CartResponse;
import com.monkcommerce.monk_commerce_backend_task.entity.CartItem;
import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import com.monkcommerce.monk_commerce_backend_task.repository.CouponRepository;
import com.monkcommerce.monk_commerce_backend_task.service.strategy.CouponStrategy;
import com.monkcommerce.monk_commerce_backend_task.service.strategy.CouponStrategyFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CouponApplyService {
    private final CouponRepository repo;
    private final CouponStrategyFactory factory;

    public CouponApplyService(CouponRepository repo, CouponStrategyFactory factory) {
        this.repo = repo;
        this.factory = factory;
    }

    public List<ApplicableCouponResponse> applicableCoupons(CartRequest cartRequest) {
        List<ApplicableCouponResponse> result = new ArrayList<>();
        List<Coupon> coupons = repo.findAll();
        for (Coupon c : coupons) {
            if (isExpired(c)) continue;
            CouponStrategy strat = factory.getStrategy(c);
            if (strat.isApplicable(cartRequest, c)) {
                result.add(strat.computeDiscount(cartRequest, c));
            }
        }
        return result;
    }

    public CartResponse applyCoupon(Long id, CartRequest cartRequest) {
        Coupon coupon = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (isExpired(coupon)) throw new RuntimeException("Coupon expired");

        CouponStrategy strat = factory.getStrategy(coupon);

        if (!strat.isApplicable(cartRequest, coupon)) {
            throw new RuntimeException("Coupon not applicable for this cart");
        }

        CartRequest updated = strat.apply(cartRequest, coupon);
        List<CartItem> items = updated.cart().items();

        double totalPrice = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        double totalDiscount = items.stream()
                .mapToDouble(CartItem::totalDiscount)
                .sum();

        double finalPrice = totalPrice - totalDiscount;

        return new CartResponse(
                updated.cart(),
                round(totalPrice),
                round(totalDiscount),
                round(finalPrice)
        );
    }


    private boolean isExpired(Coupon c) {
        if (c.getExpiresAt() == null) return false;
        return c.getExpiresAt().isBefore(LocalDate.now());
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
