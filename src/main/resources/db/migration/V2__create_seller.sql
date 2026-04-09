CREATE TABLE seller (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_seller PRIMARY KEY (id),
    CONSTRAINT uk_seller_code UNIQUE (seller_code)
);