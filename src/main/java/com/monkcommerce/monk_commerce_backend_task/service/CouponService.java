package com.monkcommerce.monk_commerce_backend_task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommerce.monk_commerce_backend_task.dto.CouponRequest;
import com.monkcommerce.monk_commerce_backend_task.dto.CouponResponse;
import com.monkcommerce.monk_commerce_backend_task.entity.Coupon;
import com.monkcommerce.monk_commerce_backend_task.exceptionHandling.CouponNotFoundException;
import com.monkcommerce.monk_commerce_backend_task.repository.CouponRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    private final CouponRepository repository;
    private final ObjectMapper objectMapper;

    public CouponService(CouponRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public CouponResponse create(CouponRequest req) {
        String detailsJsonString;
        try {
            detailsJsonString = objectMapper.writeValueAsString(req.detailsJson());
            System.out.println("Converted details JSON string: " + detailsJsonString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize coupon details.", e);
        }
        Coupon coupon = new Coupon(req.type(), detailsJsonString, req.expiresAt(), req.name());
        return CouponResponse.fromEntity(repository.save(coupon));
    }

    public List<CouponResponse> listAll() {
        return repository.findAll().stream()
            .map(CouponResponse::fromEntity)
            .toList(); }

    public CouponResponse getById(Long id) {
        Coupon coupon = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
        return CouponResponse.fromEntity(coupon);
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest req) {
        Coupon existing = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        String detailsJsonString;
        try {
            // CONVERSION STEP: Convert the JsonNode object into a raw JSON String
            detailsJsonString = objectMapper.writeValueAsString(req.detailsJson());
            System.out.println("Converted details JSON string: " + detailsJsonString);
        } catch (Exception e) {
            // This should rarely happen if JsonNode was successfully parsed, but good practice.
            throw new RuntimeException("Failed to serialize coupon details.", e);
        }

        existing.updateDetails(
                req.type(),
                detailsJsonString,
                req.expiresAt(),
                req.name()
        );

        Coupon saved = repository.save(existing);

        return CouponResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CouponNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
