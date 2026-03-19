package com.ecommerce.order.messaging;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, String reason) {}