CREATE TABLE idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    idempotency_key VARCHAR(100) NOT NULL,

    order_id BIGINT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_idempotency_user_key
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_idempotency_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);