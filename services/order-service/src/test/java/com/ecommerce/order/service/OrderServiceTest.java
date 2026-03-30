package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductView;
import com.ecommerce.order.dto.CreateOrderItemRequest;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.exception.EntityNotFoundException;
import com.ecommerce.order.messaging.OrderCreatedEvent;
import com.ecommerce.order.messaging.OrderEventPublisher;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    // ---- create() ----

    @Test
    void create_withValidProduct_savesOrderAndPublishesEvent() {
        // Arrange: prepare test data
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var product = new ProductView(productId, "Laptop", new BigDecimal("999.99"), true);
        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productId, 2)
        ));

        when(productClient.getProduct(productId)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: call the method we're testing
        var order = orderService.create(request, userId);

        // Assert: verify the result
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("1999.98"));

        // Verify: check that dependencies were called correctly
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publish(any(OrderCreatedEvent.class));
    }

    @Test
    void create_withMultipleProducts_calculatesTotalCorrectly() {
        var userId = UUID.randomUUID();
        var productA = UUID.randomUUID();
        var productB = UUID.randomUUID();

        when(productClient.getProduct(productA))
                .thenReturn(new ProductView(productA, "Laptop", new BigDecimal("1000.00"), true));
        when(productClient.getProduct(productB))
                .thenReturn(new ProductView(productB, "Mouse", new BigDecimal("25.50"), true));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productA, 1),
                new CreateOrderItemRequest(productB, 3)
        ));

        var order = orderService.create(request, userId);

        assertThat(order.getItems()).hasSize(2);
        // 1000.00 * 1 + 25.50 * 3 = 1076.50
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("1076.50"));
    }

    @Test
    void create_withInactiveProduct_throwsException() {
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var inactiveProduct = new ProductView(productId, "Old Phone", new BigDecimal("100.00"), false);

        when(productClient.getProduct(productId)).thenReturn(inactiveProduct);

        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productId, 1)
        ));

        assertThatThrownBy(() -> orderService.create(request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");

        // Order should NOT be saved or published
        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publish(any());
    }

    @Test
    void create_withNullProduct_throwsException() {
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        when(productClient.getProduct(productId)).thenReturn(null);

        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productId, 1)
        ));

        assertThatThrownBy(() -> orderService.create(request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void create_publishesEventWithCorrectData() {
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        when(productClient.getProduct(productId))
                .thenReturn(new ProductView(productId, "Keyboard", new BigDecimal("75.00"), true));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(productId, 2)
        ));

        orderService.create(request, userId);

        // Capture the event that was published
        var eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventPublisher).publish(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.total()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    // ---- get() ----

    @Test
    void get_withExistingId_returnsOrder() {
        var orderId = UUID.randomUUID();
        var order = Order.create(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = orderService.get(orderId);

        assertThat(result).isEqualTo(order);
    }

    @Test
    void get_withNonExistingId_throwsEntityNotFoundException() {
        var orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(orderId.toString());
    }

    // ---- updateOrderStatus() ----

    @Test
    void updateOrderStatus_withExistingOrder_updatesStatus() {
        var orderId = UUID.randomUUID();
        var order = Order.create(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(orderId, OrderStatus.PAID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_withNonExistingOrder_throwsException() {
        var orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.PAID))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
