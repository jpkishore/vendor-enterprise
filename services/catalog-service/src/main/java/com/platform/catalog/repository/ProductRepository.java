package com.platform.catalog.repository;

import com.platform.catalog.entity.Product;
import com.platform.catalog.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySlugIgnoreCase(String slug);

    java.util.Optional<Product> findBySkuIgnoreCase(String sku);

    java.util.Optional<Product> findBySlugIgnoreCase(String slug);

    Page<Product> findByCategoryId(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByStatus(
            ProductStatus status,
            Pageable pageable
    );

    Page<Product> findByCategoryIdAndStatus(
            Long categoryId,
            ProductStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE
            (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        """)
    Page<Product> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE
            (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        AND (
                :categoryId IS NULL
                OR p.category.id = :categoryId
            )
        AND (
                :status IS NULL
                OR p.status = :status
            )
        """)
    Page<Product> search(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );
}