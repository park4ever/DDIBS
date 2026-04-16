CREATE TABLE hold_reservation (
      id BIGINT NOT NULL AUTO_INCREMENT,
      order_id BIGINT NOT NULL,
      quantity INT NOT NULL,
      status VARCHAR(20) NOT NULL,
      expires_at DATETIME(6) NOT NULL,
      created_at DATETIME(6) NOT NULL,
      updated_at DATETIME(6) NOT NULL,
      CONSTRAINT pk_hold_reservation PRIMARY KEY (id),
      CONSTRAINT uk_hold_reservation_order_id UNIQUE (order_id),
      CONSTRAINT fk_hold_reservation_order FOREIGN KEY (order_id) REFERENCES orders (id),
      CONSTRAINT ck_hold_reservation_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_hold_reservation_status_expires_at ON hold_reservation (status, expires_at);