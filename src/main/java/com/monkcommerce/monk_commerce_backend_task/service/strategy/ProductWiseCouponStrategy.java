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
public class ProductWiseCouponStrategy implements CouponStrategy {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isApplicable(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode node = mapper.readTree(coupon.getDetailsJson());
            long productId = node.path("product_id").asLong(-1);
            return cartRequest.cart().items().stream()
                    .anyMatch(i -> i.productId() == productId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ApplicableCouponResponse computeDiscount(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode node = mapper.readTree(coupon.getDetailsJson());
            long productId = node.path("product_id").asLong(-1);
            double discountPercent = node.path("discount").asDouble(0.0);
            double discount = 0.0;
            for (CartItem it : cartRequest.cart().items()) {
                if (it.productId().longValue() == productId) {
                    discount += it.getSubtotal() * discountPercent / 100.0;
                }
            }
            return new ApplicableCouponResponse(coupon.getId(), coupon.getType().name(), round(discount));
        } catch (Exception e) {
            return new ApplicableCouponResponse(coupon.getId(), coupon.getType().name(), 0.0);
        }
    }

    @Override
    public CartRequest apply(CartRequest cartRequest, Coupon coupon) {
        try {
            JsonNode json = mapper.readTree(coupon.getDetailsJson());
            long targetId = json.path("product_id").asLong();
            double percent = json.path("discount").asDouble();

            List<CartItem> updated = new ArrayList<>();

            for (CartItem it : cartRequest.cart().items()) {
                double discount = 0;

                if (it.productId() == targetId) {
                    discount = (it.price() * it.quantity()) * (percent / 100.0);
                }

                updated.add(new CartItem(
                        it.productId(),
                        it.quantity(),
                        it.price(),
                        it.totalDiscount() + round(discount)
                ));
            }

            return new CartRequest(new Cart(updated));

        } catch (Exception e) {
            return cartRequest;
        }
    }


    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
