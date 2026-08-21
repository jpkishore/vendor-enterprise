package com.platform.catalog.service;

import com.platform.catalog.dto.product.ProductVariantCreateRequest;
import com.platform.catalog.dto.product.ProductVariantResponse;
import com.platform.catalog.dto.product.ProductVariantUpdateRequest;
import com.platform.catalog.entity.Product;
import com.platform.catalog.entity.ProductVariant;
import com.platform.catalog.entity.enums.ProductVariantStatus;
import com.platform.catalog.exception.ProductNotFoundException;
import com.platform.catalog.exception.ProductVariantAlreadyExistsException;
import com.platform.catalog.exception.ProductVariantNotFoundException;
import com.platform.catalog.repository.ProductRepository;
import com.platform.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductVariantResponse create(
            Long productId,
            ProductVariantCreateRequest request
    ) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ProductNotFoundException(productId)
                        );

        if (productVariantRepository.existsBySkuIgnoreCase(
                request.sku()
        )) {
            throw new ProductVariantAlreadyExistsException(
                    "Product variant SKU already exists"
            );
        }

        ProductVariant variant =
                new ProductVariant();

        variant.setProduct(product);
        variant.setSku(request.sku().trim());
        variant.setVariantName(request.variantName().trim());
        variant.setPrice(request.price());
        variant.setStatus(
                ProductVariantStatus.ACTIVE
        );

        ProductVariant saved =
                productVariantRepository.save(variant);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findAll(
            Long productId
    ) {

        // Make sure product exists
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return productVariantRepository
                .findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse findById(
            Long productId,
            Long variantId
    ) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                        .orElseThrow(() ->
                                new ProductVariantNotFoundException(
                                        variantId
                                )
                        );

        // Prevent accessing a variant belonging to another product
        if (!variant.getProduct()
                .getId()
                .equals(productId)) {

            throw new ProductVariantNotFoundException(
                    variantId
            );
        }

        return toResponse(variant);
    }

    public ProductVariantResponse update(
            Long productId,
            Long variantId,
            ProductVariantUpdateRequest request
    ) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                        .orElseThrow(() ->
                                new ProductVariantNotFoundException(
                                        variantId
                                )
                        );

        if (!variant.getProduct()
                .getId()
                .equals(productId)) {

            throw new ProductVariantNotFoundException(
                    variantId
            );
        }

        if (!variant.getSku()
                .equalsIgnoreCase(request.sku())
                && productVariantRepository
                .existsBySkuIgnoreCase(request.sku())) {

            throw new ProductVariantAlreadyExistsException(
                    "Product variant SKU already exists"
            );
        }

        variant.setSku(request.sku().trim());
        variant.setVariantName(
                request.variantName().trim()
        );
        variant.setPrice(request.price());

        if (request.status() != null) {
            variant.setStatus(request.status());
        }

        ProductVariant updated =
                productVariantRepository.save(variant);

        return toResponse(updated);
    }

    public void delete(
            Long productId,
            Long variantId
    ) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                        .orElseThrow(() ->
                                new ProductVariantNotFoundException(
                                        variantId
                                )
                        );

        if (!variant.getProduct()
                .getId()
                .equals(productId)) {

            throw new ProductVariantNotFoundException(
                    variantId
            );
        }

        productVariantRepository.delete(variant);
    }

    private ProductVariantResponse toResponse(
            ProductVariant variant
    ) {

        return new ProductVariantResponse(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getSku(),
                variant.getVariantName(),
                variant.getPrice(),
                variant.getStatus(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}