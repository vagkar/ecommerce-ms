package com.ecommerce.order.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, UUID userId, BigDecimal total) {
}
