# Day 26: JDBC Batching & High-Volume Inserts

## A. Concept
Groups DML statements into batch packets to minimize SQL engine round-trip latency: spring.jpa.properties.hibernate.jdbc.batch_size=50
