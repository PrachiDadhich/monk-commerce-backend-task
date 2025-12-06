package com.monkcommerce.monk_commerce_backend_task.dto;

import com.monkcommerce.monk_commerce_backend_task.entity.CartItem;
import java.util.List;

public record Cart(
        List<CartItem> items
) {}