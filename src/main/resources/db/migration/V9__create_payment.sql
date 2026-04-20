CREATE TABLE payment (
     id BIGINT NOT NULL AUTO_INCREMENT,
     order_id BIGINT NOT NULL,
     payment_code VARCHAR(20) NOT NULL,
     amount DECIMAL(15, 2) NOT NULL,
     status VARCHAR(20) NOT NULL,
     requested_at DATETIME(6) NOT NULL,
     approved_at DATETIME(6) NULL,
     failed_at DATETIME(6) NULL,
     failure_reason VARCHAR(255) NULL,
     created_at DATETIME(6) NOT NULL,
     updated_at DATETIME(6) NOT NULL,
     CONSTRAINT pk_payment PRIMARY KEY (id),
     CONSTRAINT uk_payment_order_id UNIQUE (order_id),
     CONSTRAINT uk_payment_code UNIQUE (payment_code),
     CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id),
     CONSTRAINT ck_payment_amount_non_negative CHECK (amount >= 0)
);

CREATE INDEX idx_payment_status_requested_at ON payment (status, requested_at);