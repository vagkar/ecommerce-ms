package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse get(Authentication authentication) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        return toResponse(cartService.getOrCreate(userId));
    }

    @PutMapping("/items")
    public CartResponse addOrUpdateItem(@RequestBody @Valid CartItemRequest request,
                                        Authentication authentication) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        return toResponse(cartService.addOrUpdateItem(userId, request.productId(), request.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable UUID productId, Authentication authentication) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        cartService.removeItem(userId, productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication authentication) {
        var userId = UUID.fromString((String) authentication.getPrincipal());
        cartService.clear(userId);
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> new CartItemResponse(item.getProductId(), item.getQuantity()))
                .toList();
        return new CartResponse(items);
    }
}