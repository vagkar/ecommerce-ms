package com.ecommerce.order.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastStatus(UUID orderId, String status) {
        String destination = "/topic/orders/" + orderId;
        log.info("Broadcasting status '{}' to {}", status, destination);
        messagingTemplate.convertAndSend(destination, new OrderStatusUpdate(orderId, status));
    }

    public record OrderStatusUpdate(UUID orderId, String status) {}
}