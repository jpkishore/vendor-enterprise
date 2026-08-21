package com.platform.catalog.repository;

import com.platform.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<ProductVariant> findBySkuIgnoreCase(String sku);

    List<ProductVariant> findByProductId(Long productId);
}