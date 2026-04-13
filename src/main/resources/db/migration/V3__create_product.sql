CREATE TABLE product (
     id BIGINT NOT NULL AUTO_INCREMENT,
     seller_id BIGINT NOT NULL,
     product_code VARCHAR(20) NOT NULL,
     name VARCHAR(100) NOT NULL,
     status VARCHAR(20) NOT NULL,
     created_at DATETIME(6) NOT NULL,
     updated_at DATETIME(6) NOT NULL,
     CONSTRAINT pk_product PRIMARY KEY (id),
     CONSTRAINT uk_product_code UNIQUE (product_code),
     CONSTRAINT fk_product_seller FOREIGN KEY (seller_id) REFERENCES seller (id)
);

CREATE INDEX idx_product_seller_id ON product (seller_id);