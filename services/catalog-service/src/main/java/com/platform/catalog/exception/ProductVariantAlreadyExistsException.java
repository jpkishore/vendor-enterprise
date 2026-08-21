package com.platform.catalog.exception;

public class ProductVariantAlreadyExistsException
        extends RuntimeException {

    public ProductVariantAlreadyExistsException(
            String message
    ) {
        super(message);
    }
}