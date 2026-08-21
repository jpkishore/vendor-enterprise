package com.platform.catalog.exception;

public class ProductVariantNotFoundException
        extends RuntimeException {

    public ProductVariantNotFoundException(Long id) {
        super("Product variant not found with id: " + id);
    }
}