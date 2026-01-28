package com.ecommerce.payment.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSucceededEvent(UUID orderId, UUID paymentId, BigDecimal amount) {
}
