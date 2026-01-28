package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        String status,
        BigDecimal total,
        List<OrderItemResponse> items
) {}