package com.ecommerce.order.dto;

import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        int quantity
) {}