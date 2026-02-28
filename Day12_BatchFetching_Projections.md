# Day 12: Batch Fetching & Projections

## A. Batch Fetching (`@BatchSize`)
**Batch Fetching** is an optimization strategy to resolve N+1 queries. Instead of loading child associations row-by-row, Hibernate loads them in fixed-size batches using an `IN` clause.
* **Mapping:** `@BatchSize(size = 20)`
* **SQL Output:** When iterating 100 orders, accessing the items collection triggers 5 queries (100 / 20) instead of 100 queries:
  ```sql
  SELECT * FROM order_items WHERE order_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
  ```
* **Global Configuration:** Enable globally in configuration:
  ```yaml
  spring.jpa.properties.hibernate.default_batch_fetch_size: 20
  ```

## B. DTO Projections
**Projections** map database results directly into DTOs (Data Transfer Objects) or Java records, bypassing the entity lifecycle.
* **JPQL Projection:**
  ```java
  @Query("SELECT new com.zbs.learning.domain.OrderDto(o.id, o.status) FROM Order o")
  List<OrderDto> findAllProjected();
  ```
* **Performance Benefit:** Because projections return flat Java DTO objects rather than entities, Hibernate **does not** register them in the L1 Cache. No entity snapshots are created, and no dirty checking loop runs. This yields massive memory and CPU savings for read-heavy operations.

## C. Interface Projections
Spring Data JPA supports dynamic **Interface Projections**. You define a getter-only interface:
```java
public interface OrderSummary {
    Long getId();
    String getStatus();
}
```
* **Mechanism:** Spring Data JPA generates a JDK Dynamic Proxy wrapper at runtime to map row arrays to the interface getter calls.

## D. Senior-Level Interview Q&A
### Q1: Why are JPQL class projections faster than entity queries for read-only reports?
* **Answer:** Entity queries load objects into the Persistence Context (L1 cache), generating loaded state snapshots (dirty-check comparison arrays), wrapping them in Byte Buddy proxies, and tracking their state. Projections bypass all Hibernate session caching overhead, running as raw SQL execution row mappers, saving memory and CPU execution cycles.

### Q2: What is the difference between Closed Projections and Open Projections in Spring Data?
* **Answer:**
  * **Closed Projections:** Interface getters map 1:1 to database columns. This allows Spring Data to optimize the generated SQL select statement to retrieve only those fields.
  * **Open Projections:** Interface getters contain SpEL expressions (e.g. `@Value("#{target.price * target.quantity}")`). This forces Spring Data to load the entire entity into memory first to evaluate the expression, losing SQL query optimizations.
