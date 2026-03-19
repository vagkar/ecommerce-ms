package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.messaging.OrderCreatedEvent;
import com.ecommerce.order.messaging.OrderEventPublisher;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;

    public Order create(CreateOrderRequest request) {
        var order = Order.create(request.userId());

        for (var item : request.items()) {
            var product = productClient.getProduct(item.productId());
            if (product == null || !product.active()) {
                throw new IllegalArgumentException("Product is not available: " + item.productId());
            }
            order.addItem(item.productId(), item.quantity(), product.price());
        }
        var savedOrder = orderRepository.save(order);
        orderEventPublisher.publish(new OrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotal()));
        return savedOrder;
    }

    public Order get(UUID id) {
        return orderRepository.findById(id).orElseThrow();
    }

    public Page<Order> getByUserId(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus status) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        order.updateStatus(status);
        orderRepository.save(order);
    }
}