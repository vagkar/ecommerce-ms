package com.ecommerce.payment.messaging;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, String reason) {
}
