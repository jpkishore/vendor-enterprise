CREATE TABLE payments (

    id BIGINT NOT NULL AUTO_INCREMENT,

    payment_number VARCHAR(100) NOT NULL,

    order_id BIGINT NOT NULL,

    order_number VARCHAR(100) NOT NULL,

    user_id BIGINT NOT NULL,

    amount DECIMAL(19,2) NOT NULL,

    currency VARCHAR(10) NOT NULL DEFAULT 'INR',

    payment_method VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    transaction_id VARCHAR(150),

    failure_reason VARCHAR(500),

    idempotency_key VARCHAR(150) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_payment_number
        UNIQUE (payment_number),

    CONSTRAINT uk_payment_idempotency
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT uk_payment_transaction
        UNIQUE (transaction_id)

);