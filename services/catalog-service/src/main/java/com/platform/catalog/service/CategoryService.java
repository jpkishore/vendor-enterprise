package com.platform.catalog.service;

import com.platform.catalog.dto.category.CategoryCreateRequest;
import com.platform.catalog.dto.category.CategoryResponse;
import com.platform.catalog.dto.category.CategoryUpdateRequest;
import com.platform.catalog.entity.Category;
import com.platform.catalog.entity.enums.CategoryStatus;
import com.platform.catalog.exception.CategoryAlreadyExistsException;
import com.platform.catalog.exception.CategoryNotFoundException;
import com.platform.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    @CacheEvict(
            value = "categoryAll",
            key = "'all'"
    )
    public CategoryResponse create(
            CategoryCreateRequest request
    ) {

        if (categoryRepository.existsByNameIgnoreCase(
                request.name()
        )) {
            throw new CategoryAlreadyExistsException(
                    "Category name already exists"
            );
        }

        if (categoryRepository.existsBySlugIgnoreCase(
                request.slug()
        )) {
            throw new CategoryAlreadyExistsException(
                    "Category slug already exists"
            );
        }

        Category category = new Category();

        category.setName(request.name().trim());
        category.setSlug(request.slug().trim().toLowerCase());
        category.setDescription(request.description());
        category.setStatus(CategoryStatus.ACTIVE);

        Category saved =
                categoryRepository.save(category);

        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {

        return categoryRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(
            value = "categoryById",
            key = "#id"
    )
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(id)
                        );

        return toResponse(category);
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "categoryById",
                    key = "#id"
            ),
            @CacheEvict(
                    value = "categoryAll",
                    key = "'all'"
            )
    })
    public CategoryResponse update(
            Long id,
            CategoryUpdateRequest request
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(id)
                        );

        if (!category.getName()
                .equalsIgnoreCase(request.name())
                && categoryRepository.existsByNameIgnoreCase(
                request.name()
        )) {

            throw new CategoryAlreadyExistsException(
                    "Category name already exists"
            );
        }

        if (!category.getSlug()
                .equalsIgnoreCase(request.slug())
                && categoryRepository.existsBySlugIgnoreCase(
                request.slug()
        )) {

            throw new CategoryAlreadyExistsException(
                    "Category slug already exists"
            );
        }

        category.setName(request.name().trim());
        category.setSlug(
                request.slug().trim().toLowerCase()
        );
        category.setDescription(request.description());

        if (request.status() != null) {
            category.setStatus(request.status());
        }

        return toResponse(categoryRepository.save(category));
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "categoryById",
                    key = "#id"
            ),
            @CacheEvict(
                    value = "categoryAll",
                    key = "'all'"
            )
    })
    public void delete(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(id)
                        );

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(
            Category category
    ) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}