CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    launch_variant_id BIGINT NOT NULL,
    order_code VARCHAR(20) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    variant_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_code UNIQUE (order_code),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES seller (id),
    CONSTRAINT fk_orders_launch_variant FOREIGN KEY (launch_variant_id) REFERENCES launch_variant (id),
    CONSTRAINT ck_orders_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT ck_orders_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_orders_total_price_non_negative CHECK (total_price >= 0),
    CONSTRAINT ck_orders_quantity_v1 CHECK (quantity = 1)
);

CREATE INDEX idx_orders_member_id ON orders (member_id);
CREATE INDEX idx_orders_seller_id ON orders (seller_id);
CREATE INDEX idx_orders_launch_variant_id ON orders (launch_variant_id);
CREATE INDEX idx_orders_status_created_at ON orders (status, created_at);