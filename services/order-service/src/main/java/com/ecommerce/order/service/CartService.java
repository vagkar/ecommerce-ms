package com.ecommerce.order.service;

import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getOrCreate(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));
    }

    @Transactional
    public Cart addOrUpdateItem(UUID userId, UUID productId, int quantity) {
        var cart = getOrCreate(userId);
        cart.addOrUpdateItem(productId, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(UUID userId, UUID productId) {
        var cart = getOrCreate(userId);
        cart.removeItem(productId);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clear(UUID userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }
}