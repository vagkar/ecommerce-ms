package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<CreateOrderItemRequest> items
) {}