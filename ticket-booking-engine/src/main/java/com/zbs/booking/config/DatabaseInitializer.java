package com.zbs.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (1, 'User 1', 'user1@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (2, 'User 2', 'user2@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (3, 'User 3', 'user3@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (4, 'User 4', 'user4@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (5, 'User 5', 'user5@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (6, 'User 6', 'user6@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (7, 'User 7', 'user7@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (8, 'User 8', 'user8@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (9, 'User 9', 'user9@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO users (id, name, email, created_date, last_modified_date) VALUES (10, 'User 10', 'user10@showcase.com', now(), now()) ON CONFLICT (id) DO NOTHING");
    }
}