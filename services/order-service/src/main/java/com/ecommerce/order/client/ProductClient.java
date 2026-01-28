package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${app.product-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ProductView getProduct(UUID id) {
        return restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(ProductView.class);
    }
}
