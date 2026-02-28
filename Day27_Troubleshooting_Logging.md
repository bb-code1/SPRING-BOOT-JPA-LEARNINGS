# Day 27: Production Troubleshooting & Logging

## A. Database Logging Configurations
To identify and debug slow queries or N+1 queries, configure logging parameters:
* **Log Queries:** `spring.jpa.show-sql=true`
* **Format Queries:** `spring.jpa.properties.hibernate.format_sql=true`
* **Log SQL Bind Parameters (Spring Boot 3):**
  ```properties
  logging.level.org.hibernate.orm.jdbc.bind=trace
  ```

## B. Execution Plan Analysis (`EXPLAIN ANALYZE`)
When a query executes slowly in production:
1. Capture the exact SQL statement from the log.
2. Prepend **`EXPLAIN ANALYZE`** and execute it directly in the database engine console.
3. **Execution Plan Markers:**
   * **`Seq Scan` (Table Scan):** Indicates the database is reading the entire table from disk. Resolve by adding appropriate indexes.
   * **`Index Scan`:** The database uses an index range scan. Fast and optimal.
   * **`Nested Loop`:** Indicates nested loops for joins. Can be slow if inner row counts are high.

## C. Common Production Issues
* **Connection Leaks:** Connections are acquired but never closed.
* **Lock Wait Timeout:** Transactions block waiting for row locks.
* **Out Of Memory (OOM):** Loading too many entities into the L1 Cache.

## D. Senior-Level Interview Q&A
### Q1: How do you identify N+1 query patterns in application logs?
* **Answer:** Look for a single SQL select query targeting the parent table, followed immediately by repeating SQL select statements targeting the child table with varying ID parameters (e.g. `WHERE order_id = ?`).

### Q2: Why is the `show-sql=true` setting avoided in production environments?
* **Answer:** `show-sql=true` writes output directly to standard out (`System.out`), which bypasses logging frameworks. This introduces blocking operations and high disk write latency, degrading application performance. Always use `logging.level.org.hibernate.SQL=DEBUG` instead.
