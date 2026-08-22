-- =========================================================
-- Carts
-- =========================================================

CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_carts_user
        UNIQUE (user_id)
);


-- =========================================================
-- Cart Items
-- =========================================================

CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    cart_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    variant_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_cart_items_variant
        UNIQUE (cart_id, variant_id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE
);


-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_carts_user
    ON carts(user_id);

CREATE INDEX idx_carts_status
    ON carts(status);

CREATE INDEX idx_cart_items_cart
    ON cart_items(cart_id);

CREATE INDEX idx_cart_items_product
    ON cart_items(product_id);

CREATE INDEX idx_cart_items_variant
    ON cart_items(variant_id);