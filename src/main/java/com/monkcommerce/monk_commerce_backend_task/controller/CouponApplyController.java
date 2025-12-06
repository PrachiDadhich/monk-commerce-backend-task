package com.monkcommerce.monk_commerce_backend_task.controller;

import com.monkcommerce.monk_commerce_backend_task.dto.ApplicableCouponResponse;
import com.monkcommerce.monk_commerce_backend_task.dto.CartRequest;
import com.monkcommerce.monk_commerce_backend_task.dto.CartResponse;
import com.monkcommerce.monk_commerce_backend_task.service.CouponApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Coupon Apply Controller", description = "Fetch applicable coupons and apply coupon to cart")
public class CouponApplyController {
    private final CouponApplyService service;

    public CouponApplyController(CouponApplyService service) { this.service = service; }

    @Operation(summary = "Fetch all applicable coupons for a given cart")
    @PostMapping("/applicable-coupons")
    public ResponseEntity<List<ApplicableCouponResponse>> applicable(@RequestBody CartRequest request) {
        return ResponseEntity.ok(service.applicableCoupons(request));
    }

    @Operation(summary = "Apply a specific coupon to the cart")
    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<CartResponse> apply(@PathVariable Long id, @RequestBody CartRequest request) {
        return ResponseEntity.ok(service.applyCoupon(id, request));
    }
}
