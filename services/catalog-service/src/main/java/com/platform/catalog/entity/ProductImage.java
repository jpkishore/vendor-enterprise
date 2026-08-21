package com.platform.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "product_images")
@Getter
@Setter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_images_product"
            )
    )
    private Product product;

    @Column(
            name = "image_url",
            nullable = false,
            length = 500
    )
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder = 0;

    @Column(
            name = "is_primary",
            nullable = false
    )
    private Boolean primary = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();

        if (displayOrder == null) {
            displayOrder = 0;
        }

        if (primary == null) {
            primary = false;
        }
    }
}