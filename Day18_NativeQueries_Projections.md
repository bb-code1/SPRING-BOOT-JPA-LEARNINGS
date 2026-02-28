# Day 18: Native Queries & Interface Projections

## A. Native Queries (`nativeQuery = true`)
A **Native Query** executes raw SQL directly against the underlying database engine, bypassing the JPA query translator.
* **Syntax:**
  ```java
  @Query(value = "SELECT * FROM orders WHERE status = :status", nativeQuery = true)
  List<Order> findByStatusNative(String status);
  ```
* **When to use:**
  1. Accessing database-specific SQL features (e.g. PostgreSQL Common Table Expressions (CTEs), window functions, jsonb queries).
  2. Leveraging specific query hints or execution plan optimizations.

## B. Interface Projections with Native Queries
Unlike JPQL, native queries return object arrays (`Object[]`). Spring Data solves this using dynamic **Interface Projections**:
```java
public interface OrderNativeSummary {
    Long getId();
    String getStatus();
    Double getTotalAmount();
}
```
* **SQL Alias Binding:** The database column names in the select query must match the interface getter aliases:
  ```sql
  SELECT id, status, amount AS totalAmount FROM orders;
  ```

## C. Pitfalls of Native Queries
* **Database Lock-in:** Bypasses database engine portability. Changing dialects (e.g. H2 to Oracle) breaks native queries.
* **Cache Desync:** Native write modifications bypass the Persistence Context, leaving L1 Cache entities stale.
* **No Automatic Pagination:** Offset paging for native queries requires manual count queries.

## D. Senior-Level Interview Q&A
### Q1: Does `@Query(value = "...", nativeQuery = true)` support automatic pagination?
* **Answer:** **No.** For custom native queries, Spring Data JPA cannot dynamically rewrite the SQL query to calculate count results. You must explicitly define a secondary query to handle record counts:
  ```java
  @Query(value = "SELECT * FROM orders", 
         countQuery = "SELECT count(*) FROM orders", 
         nativeQuery = true)
  Page<Order> findAllNative(Pageable pageable);
  ```

### Q2: Why do entity modifications via native queries bypass entity lifecycle callbacks?
* **Answer:** Because native SQL operations execute directly on the database engine. Since the entities do not pass through Hibernate's Persistence Context engine, JPA interceptors like `@PrePersist`, `@PostUpdate`, and entity listeners are completely bypassed.
