CREATE SEQUENCE products_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    price NUMERIC(15, 2) NOT NULL
);

-- Seed some reference products
INSERT INTO products (id, name, sku, price) VALUES (nextval('products_seq'), 'Enterprise Database Tuning Guide', 'SKU-ORACLE-01', 99.99);
INSERT INTO products (id, name, sku, price) VALUES (nextval('products_seq'), 'PL/SQL Expert Course', 'SKU-PLSQL-02', 149.50);
INSERT INTO products (id, name, sku, price) VALUES (nextval('products_seq'), 'Spring Boot Advanced Course', 'SKU-SPRING-03', 199.99);

-- Alter order_items to support the relational intermediate link
ALTER TABLE order_items ADD COLUMN product_id BIGINT;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE order_items DROP COLUMN product_name;
ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;

CREATE INDEX idx_order_items_product_id ON order_items(product_id);
