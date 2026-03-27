package com.ecommerce.order.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items
) {}