package com.ecommerce.order.controller;

import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    private static final UUID USER_ID = UUID.randomUUID();

    // Simulate authenticated request with userId as principal (matching JwtAuthFilter format)
    private static org.springframework.test.web.servlet.request.RequestPostProcessor authenticated() {
        var auth = new UsernamePasswordAuthenticationToken(
                USER_ID.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        return authentication(auth);
    }

    // ===== GET /cart =====

    @Test
    void getCart_withAuth_returns200() throws Exception {
        var cart = Cart.create(USER_ID);
        when(cartService.getOrCreate(USER_ID)).thenReturn(cart);

        mockMvc.perform(get("/cart").with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void getCart_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    // ===== PUT /cart/items =====

    @Test
    void addOrUpdateItem_withValidRequest_returns200() throws Exception {
        var productId = UUID.randomUUID();
        var cart = Cart.create(USER_ID);
        cart.addOrUpdateItem(productId, 3);

        when(cartService.addOrUpdateItem(eq(USER_ID), eq(productId), eq(3))).thenReturn(cart);

        mockMvc.perform(put("/cart/items")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": "%s", "quantity": 3}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void addOrUpdateItem_withMissingProductId_returns400() throws Exception {
        mockMvc.perform(put("/cart/items")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 3}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addOrUpdateItem_withZeroQuantity_returns400() throws Exception {
        mockMvc.perform(put("/cart/items")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": "%s", "quantity": 0}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE /cart/items/{productId} =====

    @Test
    void removeItem_withAuth_returns204() throws Exception {
        var productId = UUID.randomUUID();
        var cart = Cart.create(USER_ID);
        when(cartService.removeItem(USER_ID, productId)).thenReturn(cart);

        mockMvc.perform(delete("/cart/items/{productId}", productId)
                        .with(authenticated()))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(USER_ID, productId);
    }

    // ===== DELETE /cart =====

    @Test
    void clear_withAuth_returns204() throws Exception {
        mockMvc.perform(delete("/cart").with(authenticated()))
                .andExpect(status().isNoContent());

        verify(cartService).clear(USER_ID);
    }

    @Test
    void clear_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/cart"))
                .andExpect(status().isUnauthorized());
    }
}
