package com.ecommerce.order.client;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductView(
        UUID id,
        String name,
        BigDecimal price,
        boolean active
) {}