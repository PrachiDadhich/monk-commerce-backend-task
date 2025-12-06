package com.monkcommerce.monk_commerce_backend_task.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.monkcommerce.monk_commerce_backend_task.entity.enums.CouponType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CouponRequest(
        @NotNull(message = "Coupon type cannot be null")
        CouponType type,

        @NotBlank(message = "Details JSON cannot be empty")
        JsonNode detailsJson,

        @NotNull(message = "Expiration date cannot be null")
        @FutureOrPresent(message = "Expiration date must be today or in the future")
        LocalDate expiresAt,

        @NotBlank(message = "Coupon name cannot be empty")
        @Size(min = 3, max = 50, message = "Coupon name must be between 3 and 50 characters")
        String name
) {}