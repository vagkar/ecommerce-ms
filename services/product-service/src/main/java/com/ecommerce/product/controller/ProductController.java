package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.UpdateProductRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody @Valid CreateProductRequest request) {
        return toResponse(productService.create(request));
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return toResponse(productService.get(id));
    }

    @GetMapping
    public List<ProductResponse> all() {
        return productService.list().stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateProductRequest request) {
        return toResponse(productService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive()
        );
    }
}