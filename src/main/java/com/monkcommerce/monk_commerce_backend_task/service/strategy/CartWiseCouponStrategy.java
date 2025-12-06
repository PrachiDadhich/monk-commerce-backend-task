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

@Component
public class CartWiseCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isApplicable(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode node = mapper.readTree(coupon.getDetailsJson());
            double threshold = node.path("threshold").asDouble(0.0);
            double total = cartTotal(cartRequest);
            return total >= threshold;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ApplicableCouponResponse computeDiscount(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode node = mapper.readTree(coupon.getDetailsJson());
            double discountPercent = node.path("discount").asDouble(0.0);
            double total = cartTotal(cartRequest);
            double discount = total * discountPercent / 100.0;
            return new ApplicableCouponResponse(coupon.getId(), coupon.getType().name(), discount);
        } catch (Exception e) {
            return new ApplicableCouponResponse(coupon.getId(), coupon.getType().name(), 0.0);
        }
    }

    @Override
    public CartRequest apply(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode json = mapper.readTree(coupon.getDetailsJson());
            double threshold = json.path("threshold").asDouble();
            double percent = json.path("discount").asDouble();

            double totalPrice = cartRequest.cart().items().stream()
                    .mapToDouble(CartItem::getSubtotal)
                    .sum();

            if (totalPrice < threshold) return cartRequest;

            double discountTotal = totalPrice * (percent / 100.0);

            List<CartItem> updated = new ArrayList<>();

            for (CartItem it : cartRequest.cart().items()) {
                double itemShare = (it.getSubtotal() / totalPrice) * discountTotal;
                updated.add(new CartItem(
                        it.productId(),
                        it.quantity(),
                        it.price(),
                        round(it.totalDiscount() + itemShare)
                ));
            }

            return new CartRequest(new Cart(updated));

        } catch (Exception e) {
            return cartRequest;
        }
    }


    private double cartTotal(CartRequest cartRequest) {
        return cartRequest.cart().items().stream()
                .mapToDouble(CartItem::getSubtotal).sum();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}