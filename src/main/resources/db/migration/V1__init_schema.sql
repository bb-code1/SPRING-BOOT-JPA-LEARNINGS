CREATE SEQUENCE orders_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE order_items_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    order_id BIGINT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
