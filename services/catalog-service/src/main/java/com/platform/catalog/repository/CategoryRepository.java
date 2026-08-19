package com.platform.catalog.repository;

import com.platform.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(String slug);

    Optional<Category> findBySlugIgnoreCase(String slug);

    List<Category> findByStatusOrderByNameAsc(
            com.platform.catalog.entity.enums.CategoryStatus status
    );
}