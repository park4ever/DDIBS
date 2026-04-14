CREATE TABLE launch_variant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    launch_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    sale_price DECIMAL(15, 2) NOT NULL,
    total_stock INT NOT NULL,
    available_stock INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_launch_variant PRIMARY KEY (id),
    CONSTRAINT uk_launch_variant_launch_id_product_variant_id UNIQUE (launch_id, product_variant_id),
    CONSTRAINT fk_launch_variant_launch FOREIGN KEY (launch_id) REFERENCES launch (id),
    CONSTRAINT fk_launch_variant_product_variant FOREIGN KEY (product_variant_id) REFERENCES product_variant (id),
    CONSTRAINT ck_launch_variant_total_stock_positive CHECK (total_stock >= 0),
    CONSTRAINT ck_launch_variant_available_stock_range CHECK (available_stock >= 0 AND available_stock <= total_stock),
    CONSTRAINT ck_launch_variant_sale_price_positive CHECK (sale_price >= 0)
);

CREATE INDEX idx_launch_variant_launch_id ON launch_variant (launch_id);
CREATE INDEX idx_launch_variant_product_variant_id ON launch_variant (product_variant_id);
CREATE INDEX idx_launch_variant_available_stock ON launch_variant (available_stock);