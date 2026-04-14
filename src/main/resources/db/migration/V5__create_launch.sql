CREATE TABLE launch (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    launch_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_launch PRIMARY KEY (id),
    CONSTRAINT uk_launch_code UNIQUE (launch_code),
    CONSTRAINT fk_launch_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT ck_launch_period CHECK (start_at < end_at)
);

CREATE INDEX idx_launch_product_id ON launch (product_id);
CREATE INDEX idx_launch_status_start_at_end_at ON launch (status, start_at, end_at);