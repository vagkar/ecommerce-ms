package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrderRequest request,
                                Authentication authentication) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        return toResponse(orderService.create(request, userId));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return toResponse(orderService.get(id));
    }

    @GetMapping
    public Page<OrderResponse> getByUser(Authentication authentication, Pageable pageable) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        return orderService.getByUserId(userId, pageable).map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        var items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getStatus().name(), order.getTotal(), items);
    }
}