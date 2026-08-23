package com.platform.catalog.service;

import com.platform.catalog.dto.product.ProductCreateRequest;
import com.platform.catalog.dto.product.ProductPageResponse;
import com.platform.catalog.dto.product.ProductResponse;
import com.platform.catalog.dto.product.ProductUpdateRequest;
import com.platform.catalog.entity.Category;
import com.platform.catalog.entity.Product;
import com.platform.catalog.entity.enums.ProductStatus;
import com.platform.catalog.exception.CategoryNotFoundException;
import com.platform.catalog.exception.ProductAlreadyExistsException;
import com.platform.catalog.exception.ProductNotFoundException;
import com.platform.catalog.repository.CategoryRepository;
import com.platform.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(
            ProductCreateRequest request
    ) {

        // Check SKU
        if (productRepository.existsBySkuIgnoreCase(
                request.sku()
        )) {
            throw new ProductAlreadyExistsException(
                    "Product SKU already exists"
            );
        }

        // Check slug
        if (productRepository.existsBySlugIgnoreCase(
                request.slug()
        )) {
            throw new ProductAlreadyExistsException(
                    "Product slug already exists"
            );
        }

        // Check category
        Category category =
                categoryRepository.findById(
                        request.categoryId()
                ).orElseThrow(() ->
                        new CategoryNotFoundException(
                                request.categoryId()
                        )
                );

        Product product = new Product();

        product.setCategory(category);
        product.setName(request.name().trim());
        product.setSlug(
                request.slug().trim().toLowerCase()
        );
        product.setDescription(request.description());
        product.setSku(request.sku().trim());

        product.setStatus(ProductStatus.ACTIVE);

        Product saved =
                productRepository.save(product);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {

        return productRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(
            value = "productById",
            key = "#id"
    )
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(id)
                        );

        return toResponse(product);
    }

    @CacheEvict(
            value = "productById",
            key = "#id"
    )
    public ProductResponse update(
            Long id,
            ProductUpdateRequest request
    ) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(id)
                        );

        // Check SKU only if changed
        if (!product.getSku()
                .equalsIgnoreCase(request.sku())
                && productRepository.existsBySkuIgnoreCase(
                request.sku()
        )) {

            throw new ProductAlreadyExistsException(
                    "Product SKU already exists"
            );
        }

        // Check slug only if changed
        if (!product.getSlug()
                .equalsIgnoreCase(request.slug())
                && productRepository.existsBySlugIgnoreCase(
                request.slug()
        )) {

            throw new ProductAlreadyExistsException(
                    "Product slug already exists"
            );
        }

        Category category =
                categoryRepository.findById(
                        request.categoryId()
                ).orElseThrow(() ->
                        new CategoryNotFoundException(
                                request.categoryId()
                        )
                );

        product.setCategory(category);
        product.setName(request.name().trim());
        product.setSlug(
                request.slug().trim().toLowerCase()
        );
        product.setDescription(request.description());
        product.setSku(request.sku().trim());

        if (request.status() != null) {
            product.setStatus(request.status());
        }

        Product updated =
                productRepository.save(product);

        return toResponse(updated);
    }

    @CacheEvict(
            value = "productById",
            key = "#id"
    )
    public void delete(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(id)
                        );

        productRepository.delete(product);
    }

    private ProductResponse toResponse(
            Product product
    ) {

        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getSku(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProductPageResponse findAll(
            int page,
            int size,
            String sortBy,
            String direction,
            Long categoryId,
            ProductStatus status
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(sortDirection, sortBy)
                );

        Page<Product> products;

        if (categoryId != null && status != null) {

            products =
                    productRepository
                            .findByCategoryIdAndStatus(
                                    categoryId,
                                    status,
                                    pageable
                            );

        } else if (categoryId != null) {

            products =
                    productRepository.findByCategoryId(
                            categoryId,
                            pageable
                    );

        } else if (status != null) {

            products =
                    productRepository.findByStatus(
                            status,
                            pageable
                    );

        } else {

            products =
                    productRepository.findAll(pageable);
        }

        return new ProductPageResponse(
                products.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ProductPageResponse search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction,
            Long categoryId,
            ProductStatus status
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(sortDirection, sortBy)
                );

        String searchKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        Page<Product> products =
                productRepository.search(
                        searchKeyword,
                        categoryId,
                        status,
                        pageable
                );

        return new ProductPageResponse(
                products.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }
}