package com.platform.catalog.exception;

public class ProductImageNotFoundException
        extends RuntimeException {

    public ProductImageNotFoundException(Long id) {
        super("Product image not found with id: " + id);
    }
}