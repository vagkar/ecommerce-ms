package com.ecommerce.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsListener {

    private final PaymentEventPublisher paymentEventPublisher;

    @KafkaListener(topics = "order.events", groupId = "payment-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        boolean success = ThreadLocalRandom.current().nextInt(100) < 80; // 80% chance of success

        if (success) {
            paymentEventPublisher.publishSucceeded(new PaymentSucceededEvent(event.orderId(), UUID.randomUUID(), event.total()));
            log.info("Payment succeeded for order: {}", event.orderId());
        } else {
            paymentEventPublisher.publishFailed(new PaymentFailedEvent(event.orderId(), "Insufficient funds"));
            log.info("Payment failed for order: {}", event.orderId());
        }
    }
}
