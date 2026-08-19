-- =========================================================
-- Categories
-- =========================================================

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description VARCHAR(500),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_categories_name
        UNIQUE (name),

    CONSTRAINT uk_categories_slug
        UNIQUE (slug)
);


-- =========================================================
-- Products
-- =========================================================

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,

    category_id BIGINT NOT NULL,

    name VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL,

    description TEXT,

    sku VARCHAR(100) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_products_sku
        UNIQUE (sku),

    CONSTRAINT uk_products_slug
        UNIQUE (slug),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);


-- =========================================================
-- Product Variants
-- =========================================================

CREATE TABLE product_variants (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    sku VARCHAR(100) NOT NULL,

    variant_name VARCHAR(150) NOT NULL,

    price DECIMAL(12,2) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_product_variants_sku
        UNIQUE (sku),

    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);


-- =========================================================
-- Product Images
-- =========================================================

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    image_url VARCHAR(500) NOT NULL,

    alt_text VARCHAR(255),

    display_order INT NOT NULL DEFAULT 0,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);


-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_products_category
    ON products(category_id);

CREATE INDEX idx_products_status
    ON products(status);

CREATE INDEX idx_product_variants_product
    ON product_variants(product_id);

CREATE INDEX idx_product_images_product
    ON product_images(product_id);