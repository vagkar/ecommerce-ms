package com.ecommerce.order.messaging;

import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.websocket.OrderStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventsListener {

    private final OrderService orderService;
    private final OrderStatusBroadcaster orderStatusBroadcaster;

    @KafkaListener(topics = "${app.kafka.topics.payment-succeeded}", groupId = "order-service-payment")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Payment succeeded for order: {}", event.orderId());
        orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);
        orderStatusBroadcaster.broadcastStatus(event.orderId(), OrderStatus.PAID.name());
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}", groupId = "order-service-payment")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Payment failed for order: {}, reason: {}", event.orderId(), event.reason());
        orderService.updateOrderStatus(event.orderId(), OrderStatus.PAYMENT_FAILED);
        orderStatusBroadcaster.broadcastStatus(event.orderId(), OrderStatus.PAYMENT_FAILED.name());
    }
}