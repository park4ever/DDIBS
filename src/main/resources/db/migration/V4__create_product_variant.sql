CREATE TABLE product_variant (
     id BIGINT NOT NULL AUTO_INCREMENT,
     product_id BIGINT NOT NULL,
     variant_code VARCHAR(20) NOT NULL,
     name VARCHAR(100) NOT NULL,
     status VARCHAR(20) NOT NULL,
     created_at DATETIME(6) NOT NULL,
     updated_at DATETIME(6) NOT NULL,
     CONSTRAINT pk_product_variant PRIMARY KEY (id),
     CONSTRAINT uk_product_variant_code UNIQUE (variant_code),
     CONSTRAINT uk_product_variant_product_id_name UNIQUE (product_id, name),
     CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE INDEX idx_product_variant_product_id ON product_variant (product_id);