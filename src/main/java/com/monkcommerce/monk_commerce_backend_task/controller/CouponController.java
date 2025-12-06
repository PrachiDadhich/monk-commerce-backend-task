package com.monkcommerce.monk_commerce_backend_task.controller;

import com.monkcommerce.monk_commerce_backend_task.dto.CouponRequest;
import com.monkcommerce.monk_commerce_backend_task.dto.CouponResponse;
import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import com.monkcommerce.monk_commerce_backend_task.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Coupon CRUD Controller", description = "Create, Read, Update, Delete coupons")
public class CouponController {
    private final CouponService service;

    public CouponController(CouponService service) { this.service = service; }

    @Operation(summary = "Create a new coupon")
    @PostMapping("/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest req) {
        CouponResponse saved = service.create(req);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Get all coupons")
    @GetMapping("/coupons")
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @Operation(summary = "Get coupon by ID")
    @GetMapping("/coupons/{id}")
    public ResponseEntity<CouponResponse> getOne(@PathVariable Long id) {
        CouponResponse response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update coupon by ID")
    @PutMapping("/coupons/{id}")
    public ResponseEntity<CouponResponse> update(@PathVariable Long id, @RequestBody CouponRequest req) {
        CouponResponse savedResponse = service.update(id, req);
        return ResponseEntity.ok(savedResponse);
    }

    @Operation(summary = "Delete coupon by ID")
    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
