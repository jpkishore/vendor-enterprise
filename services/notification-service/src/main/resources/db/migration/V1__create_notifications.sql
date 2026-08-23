CREATE TABLE notifications (

    id BIGINT NOT NULL AUTO_INCREMENT,

    notification_type VARCHAR(50) NOT NULL,

    recipient_user_id BIGINT NOT NULL,

    order_id BIGINT NULL,

    order_number VARCHAR(100) NULL,

    payment_id BIGINT NULL,

    subject VARCHAR(255) NULL,

    message TEXT NOT NULL,

    status VARCHAR(20) NOT NULL,

    retry_count INT NOT NULL DEFAULT 0,

    failure_reason VARCHAR(500) NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    sent_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_notifications_user_id (recipient_user_id),

    INDEX idx_notifications_order_id (order_id),

    INDEX idx_notifications_status (status)

);