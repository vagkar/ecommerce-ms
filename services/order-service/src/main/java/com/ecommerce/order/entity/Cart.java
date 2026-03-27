package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<CartItem> items = new ArrayList<>();

    public static Cart create(UUID userId) {
        var cart = new Cart();
        cart.userId = userId;
        return cart;
    }

    public void addOrUpdateItem(UUID productId, int quantity) {
        var existing = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().updateQuantity(quantity);
        } else {
            items.add(new CartItem(this, productId, quantity));
        }
    }

    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clear() {
        items.clear();
    }
}