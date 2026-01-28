package com.ecommerce.product.service;

import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(CreateProductRequest request) {
        productRepository.findBySku(request.sku()).ifPresent(p -> {
            throw new IllegalArgumentException("Product with SKU " + request.sku() + " already exists.");
        });
        return productRepository.save(new Product(request.sku(), request.name(), request.price()));
    }

    public List<Product> list() {
        return productRepository.findAll();
    }

    public Product get(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product with ID " + id + " not found."));
    }
}
