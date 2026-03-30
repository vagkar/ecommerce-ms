package com.ecommerce.order.service;

import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    // ===== getOrCreate — user without cart =====

    @Test
    void getOrCreate_withNewUser_createsNewCart() {
        // Arrange: user has no cart — findByUserId returns empty
        var userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var cart = cartService.getOrCreate(userId);

        // Assert: new cart created with correct userId and empty items
        assertThat(cart.getUserId()).isEqualTo(userId);
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    // ===== getOrCreate — user with existing cart =====

    @Test
    void getOrCreate_withExistingUser_returnsExistingCart() {
        // Arrange: user already has a cart
        var userId = UUID.randomUUID();
        var existingCart = Cart.create(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // Act
        var cart = cartService.getOrCreate(userId);

        // Assert: returned existing cart, no new one created
        assertThat(cart).isEqualTo(existingCart);
        verify(cartRepository, never()).save(any());
    }

    // ===== addOrUpdateItem — new item =====

    @Test
    void addOrUpdateItem_withNewProduct_addsItemToCart() {
        // Arrange: user has an empty cart
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var cart = Cart.create(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var resultCart = cartService.addOrUpdateItem(userId, productId, 3);

        // Assert: cart has 1 item with correct productId and quantity
        assertThat(resultCart).isEqualTo(cart);
        assertThat(cart.getItems()).hasSize(1);
        var item = cart.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(3);
        verify(cartRepository).save(any(Cart.class));
    }

    // ===== addOrUpdateItem — update quantity =====

    @Test
    void addOrUpdateItem_withExistingProduct_updatesQuantity() {
        // Arrange: cart already has 1 item with quantity=2
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var cart = Cart.create(userId);
        cart.addOrUpdateItem(productId, 2);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act: update quantity to 5
        var resultCart = cartService.addOrUpdateItem(userId, productId, 5);

        // Assert: still 1 item (no duplicate), quantity updated to 5
        assertThat(resultCart).isEqualTo(cart);
        assertThat(cart.getItems()).hasSize(1);
        var item = cart.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(5);
        verify(cartRepository).save(any(Cart.class));
    }

    // ===== removeItem =====

    @Test
    void removeItem_withExistingProduct_removesFromCart() {
        // Arrange: cart has 1 item
        var userId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var cart = Cart.create(userId);
        cart.addOrUpdateItem(productId, 1);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var resultCart = cartService.removeItem(userId, productId);

        // Assert: cart is now empty
        assertThat(resultCart).isEqualTo(cart);
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    // ===== clear =====

    @Test
    void clear_withExistingCart_removesAllItems() {
        // Arrange: cart has 2 items
        var userId = UUID.randomUUID();
        var cart = Cart.create(userId);
        cart.addOrUpdateItem(UUID.randomUUID(), 1);
        cart.addOrUpdateItem(UUID.randomUUID(), 2);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        cartService.clear(userId);

        // Assert: cart is empty, save was called
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clear_withNoCart_doesNothing() {
        // Arrange: user has no cart
        var userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        cartService.clear(userId);

        // Assert: save was never called
        verify(cartRepository, never()).save(any(Cart.class));
    }
}
