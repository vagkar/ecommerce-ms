package com.ecommerce.product.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, UUID id) {
        super(entity + " with ID " + id + " not found.");
    }
}