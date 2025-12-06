package com.monkcommerce.monk_commerce_backend_task.dto;

public record CartResponse(
        Cart updatedCart,
        double totalPrice,
        double totalDiscount,
        double finalPrice
) {
    public static CartResponse fromCalculations(
            Cart cart,
            double totalPrice,
            double totalDiscount,
            double finalPrice
    ) {
        return new CartResponse(cart, totalPrice, totalDiscount, finalPrice);
    }
}
