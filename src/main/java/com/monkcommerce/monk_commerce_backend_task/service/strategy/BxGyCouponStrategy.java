package com.monkcommerce.monk_commerce_backend_task.service.strategy;

import com.monkcommerce.monk_commerce_backend_task.dto.ApplicableCouponResponse;
import com.monkcommerce.monk_commerce_backend_task.dto.Cart;
import com.monkcommerce.monk_commerce_backend_task.dto.CartRequest;
import com.monkcommerce.monk_commerce_backend_task.entity.CartItem;
import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BxGyCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isApplicable(CartRequest cartRequest, Coupon coupon) {
        return computeApplicableTimes(cartRequest, coupon) > 0;
    }

    @Override
    public ApplicableCouponResponse computeDiscount(CartRequest cartRequest, Coupon coupon) {
        try {
            int times = computeApplicableTimes(cartRequest, coupon);
            if (times <= 0) {
                return new ApplicableCouponResponse(
                        coupon.getId(), coupon.getType().name(), 0.0
                );
            }

            JsonNode node = mapper.readTree(coupon.getDetailsJson());

            // Parse get_products as simple array
            List<Long> getProductIds = new ArrayList<>();
            for (JsonNode gp : node.path("get_products")) {
                getProductIds.add(gp.asLong());
            }

            // Build cart map for quick lookup
            Map<Long, CartItem> cartMap = cartRequest.cart().items().stream()
                    .collect(Collectors.toMap(CartItem::productId, ci -> ci));

            double discount = 0.0;

            // For each GET product → check if exists in cart
            for (Long freeProductId : getProductIds) {
                CartItem item = cartMap.get(freeProductId);

                if (item != null) {
                    // 1 free per repetition
                    int freeCount = Math.min(item.quantity(), times);

                    discount += freeCount * item.price();
                }
            }

            return new ApplicableCouponResponse(
                    coupon.getId(), coupon.getType().name(), round(discount)
            );

        } catch (Exception e) {
            return new ApplicableCouponResponse(
                    coupon.getId(), coupon.getType().name(), 0.0
            );
        }
    }


    @Override
    public CartRequest apply(CartRequest cartRequest, Coupon coupon) {
        try {
            int times = computeApplicableTimes(cartRequest, coupon);
            if (times <= 0) return cartRequest;

            JsonNode json = mapper.readTree(coupon.getDetailsJson());

            List<Long> freeProducts = new ArrayList<>();
            for (JsonNode n : json.path("get_products")) freeProducts.add(n.asLong());

            List<CartItem> updated = new ArrayList<>();

            for (CartItem it : cartRequest.cart().items()) {
                double discount = 0;

                if (freeProducts.contains(it.productId())) {
                    int freeCount = Math.min(it.quantity(), times);
                    discount = freeCount * it.price();
                }

                updated.add(new CartItem(
                        it.productId(),
                        it.quantity(),
                        it.price(),
                        it.totalDiscount() + discount
                ));
            }

            return new CartRequest(new Cart(updated));

        } catch (Exception e) {
            return cartRequest;
        }
    }


    private int computeApplicableTimes(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode node = mapper.readTree(coupon.getDetailsJson());

            List<Long> buyIds = new ArrayList<>();
            for (JsonNode bp : node.path("buy_products")) {
                buyIds.add(bp.asLong());
            }

            int cutoff = node.path("cutoff").asInt(1);
            int repetitionLimit = node.path("repetition_limit").asInt(Integer.MAX_VALUE);

            int totalBuyQty = cartRequest.cart().items().stream()
                    .filter(i -> buyIds.contains(i.productId()))
                    .mapToInt(CartItem::quantity)
                    .sum();

            if (totalBuyQty < cutoff) return 0;

            int times = totalBuyQty / cutoff;

            return Math.min(times, repetitionLimit);

        } catch (Exception e) {
            return 0;
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
