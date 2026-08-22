package com.platform.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "order_items"
)
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            name = "variant_id",
            nullable = false
    )
    private Long variantId;

    @Column(
            nullable = false
    )
    private Integer quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "total_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalPrice;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Version
    @Column(
            nullable = false
    )
    private Long version;

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = Instant.now();
    }
}