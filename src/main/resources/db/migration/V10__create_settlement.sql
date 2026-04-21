CREATE TABLE settlement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    settlement_code VARCHAR(20) NOT NULL,
    settlement_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    settled_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_settlement PRIMARY KEY (id),
    CONSTRAINT uk_settlement_order_id UNIQUE (order_id),
    CONSTRAINT uk_settlement_code UNIQUE (settlement_code),
    CONSTRAINT fk_settlement_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_settlement_seller FOREIGN KEY (seller_id) REFERENCES seller (id),
    CONSTRAINT ck_settlement_amount_non_negative CHECK (settlement_amount >= 0)
);

CREATE INDEX idx_settlement_seller_id ON settlement (seller_id);
CREATE INDEX idx_settlement_status_created_at ON settlement (status, created_at);