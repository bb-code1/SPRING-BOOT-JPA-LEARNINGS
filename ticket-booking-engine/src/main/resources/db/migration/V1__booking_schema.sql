CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE inventories_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE bookings_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE tickets_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE payments_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

CREATE TABLE user_preferences (
    user_id BIGINT NOT NULL,
    pref_key VARCHAR(100) NOT NULL,
    pref_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, pref_key)
);

CREATE TABLE events (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

CREATE TABLE event_inventories (
    id BIGINT PRIMARY KEY,
    event_id BIGINT NOT NULL UNIQUE,
    total_available INTEGER NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

CREATE TABLE bookings (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

CREATE TABLE tickets (
    id BIGINT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    price NUMERIC(15, 2) NOT NULL
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    card_number VARCHAR(100),
    card_holder VARCHAR(255),
    email VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

-- Foreign Key Constraints
ALTER TABLE event_inventories ADD CONSTRAINT fk_inventories_event FOREIGN KEY (event_id) REFERENCES events(id);
ALTER TABLE bookings ADD CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;
ALTER TABLE payments ADD CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id);

-- Indexes for performance
CREATE INDEX idx_inventories_event_id ON event_inventories(event_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_tickets_booking_id ON tickets(booking_id);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);

-- Seed Data for Concert Events
INSERT INTO events (id, name, location, event_date, created_date, last_modified_date) 
VALUES (nextval('events_seq'), 'Taylor Swift Eras Tour', 'MetLife Stadium, NJ', '2026-06-15 19:30:00', now(), now());

INSERT INTO events (id, name, location, event_date, created_date, last_modified_date) 
VALUES (nextval('events_seq'), 'Coldplay Concert', 'Wembley Stadium, London', '2026-07-20 20:00:00', now(), now());

INSERT INTO events (id, name, location, event_date, created_date, last_modified_date) 
VALUES (nextval('events_seq'), 'Metallica World Tour', 'Soldier Field, Chicago', '2026-08-05 18:00:00', now(), now());

-- Seed Inventories (e.g. 50 seats for Taylor Swift, 100 for Coldplay, 0 for Metallica to test out of stock)
INSERT INTO event_inventories (id, event_id, total_available, version, created_date, last_modified_date) 
VALUES (nextval('inventories_seq'), 1, 50, 0, now(), now());

INSERT INTO event_inventories (id, event_id, total_available, version, created_date, last_modified_date) 
VALUES (nextval('inventories_seq'), 2, 100, 0, now(), now());

INSERT INTO event_inventories (id, event_id, total_available, version, created_date, last_modified_date) 
VALUES (nextval('inventories_seq'), 3, 0, 0, now(), now());
