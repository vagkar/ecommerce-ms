package com.ecommerce.payment.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderEventsListener {

    private PaymentEventPublisher paymentEventPublisher;

    public OrderEventsListener(PaymentEventPublisher paymentEventPublisher) {
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @KafkaListener(topics = "order.events", groupId = "payment-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        boolean success = ThreadLocalRandom.current().nextInt(100) < 80; // 80% chance of success

        if (success) {
            paymentEventPublisher.publishSucceeded(new PaymentSucceededEvent(event.orderId(), UUID.randomUUID(), event.total()));
            System.out.println("PAYMENT SUCCEEDED for Order ID: " + event.orderId());
        } else {
            paymentEventPublisher.publishFailed(new PaymentFailedEvent(event.orderId(), "Insufficient funds"));
            System.out.println("PAYMENT FAILED for Order ID: " + event.orderId());
        }
        System.out.println("Received Order Created Event: " + event);
    }
}
