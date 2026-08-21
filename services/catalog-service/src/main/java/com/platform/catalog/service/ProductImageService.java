package com.platform.catalog.service;

import com.platform.catalog.dto.product.ProductImageCreateRequest;
import com.platform.catalog.dto.product.ProductImageResponse;
import com.platform.catalog.entity.Product;
import com.platform.catalog.entity.ProductImage;
import com.platform.catalog.exception.ProductImageNotFoundException;
import com.platform.catalog.exception.ProductNotFoundException;
import com.platform.catalog.repository.ProductImageRepository;
import com.platform.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageResponse create(
            Long productId,
            ProductImageCreateRequest request
    ) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ProductNotFoundException(productId)
                        );

        ProductImage image = new ProductImage();

        image.setProduct(product);
        image.setImageUrl(request.imageUrl().trim());
        image.setAltText(request.altText());

        image.setDisplayOrder(
                request.displayOrder() != null
                        ? request.displayOrder()
                        : 0
        );

        image.setPrimary(
                request.primary() != null
                        && request.primary()
        );

        ProductImage saved =
                productImageRepository.save(image);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findAll(
            Long productId
    ) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(
            Long productId,
            Long imageId
    ) {

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new ProductImageNotFoundException(
                                        imageId
                                )
                        );

        if (!image.getProduct()
                .getId()
                .equals(productId)) {

            throw new ProductImageNotFoundException(
                    imageId
            );
        }

        productImageRepository.delete(image);
    }

    private ProductImageResponse toResponse(
            ProductImage image
    ) {

        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getAltText(),
                image.getDisplayOrder(),
                image.getPrimary(),
                image.getCreatedAt()
        );
    }
}