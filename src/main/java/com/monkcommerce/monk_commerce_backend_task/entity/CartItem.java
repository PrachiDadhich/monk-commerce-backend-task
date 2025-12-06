package com.monkcommerce.monk_commerce_backend_task.entity;

public record CartItem(
        Long productId,
        int quantity,
        double price,
        double totalDiscount
) {
    public double getSubtotal() {
        return price() * quantity();
    }

}
