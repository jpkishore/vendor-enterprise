CREATE TABLE inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,

    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_inventory_variant
        UNIQUE (variant_id),

    CONSTRAINT chk_inventory_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_inventory_reserved_quantity
        CHECK (reserved_quantity >= 0),

    CONSTRAINT chk_inventory_reorder_level
        CHECK (reorder_level >= 0)
);

CREATE INDEX idx_inventory_product
    ON inventory(product_id);

CREATE INDEX idx_inventory_variant
    ON inventory(variant_id);

CREATE INDEX idx_inventory_status
    ON inventory(status);