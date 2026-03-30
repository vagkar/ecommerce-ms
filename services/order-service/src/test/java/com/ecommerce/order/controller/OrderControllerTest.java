package com.ecommerce.order.controller;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.exception.EntityNotFoundException;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private static final UUID USER_ID = UUID.randomUUID();

    // Simulate authenticated request matching JwtAuthFilter format
    private static RequestPostProcessor authenticated() {
        var auth = new UsernamePasswordAuthenticationToken(
                USER_ID.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        return authentication(auth);
    }

    // ===== POST /orders =====

    @Test
    void createOrder_withValidRequest_returns201() throws Exception {
        var productId = UUID.randomUUID();
        var order = Order.create(USER_ID);

        when(orderService.create(any(), any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": "%s", "quantity": 2}]}
                                """.formatted(productId)))
                .andExpect(status().isCreated())   // 201, not 200
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()));
    }

    @Test
    void createOrder_withEmptyItems_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": []}
                                """))
                .andExpect(status().isBadRequest());  // @NotEmpty on items
    }

    @Test
    void createOrder_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": "%s", "quantity": 1}]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_withUnavailableProduct_returns400() throws Exception {
        when(orderService.create(any(), any()))
                .thenThrow(new IllegalArgumentException("Product is not available"));

        mockMvc.perform(post("/orders")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": "%s", "quantity": 1}]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product is not available"));
    }

    // ===== GET /orders/{id} =====

    @Test
    void getOrder_withExistingId_returns200() throws Exception {
        var orderId = UUID.randomUUID();
        var order = Order.create(USER_ID);

        when(orderService.get(orderId)).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_withNonExistingId_returns404() throws Exception {
        var orderId = UUID.randomUUID();

        when(orderService.get(orderId))
                .thenThrow(new EntityNotFoundException("Order", orderId));

        mockMvc.perform(get("/orders/{id}", orderId)
                        .with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}