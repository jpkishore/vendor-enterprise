CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_number VARCHAR(50) NOT NULL,

    user_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    total_amount DECIMAL(19,2) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),

    INDEX idx_orders_user_id (user_id),

    INDEX idx_orders_status (status)
);

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    variant_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    unit_price DECIMAL(19,2) NOT NULL,

    total_price DECIMAL(19,2) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    INDEX idx_order_items_order_id (order_id),

    INDEX idx_order_items_product_id (product_id),

    INDEX idx_order_items_variant_id (variant_id)
);