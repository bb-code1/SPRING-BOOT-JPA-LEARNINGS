# Day 24: Auditing & Soft Deletes

## A. JPA Auditing
JPA Auditing automates capturing entity creation and modification metadata.
* **Metadata annotations:** `@CreatedBy`, `@CreatedDate`, `@LastModifiedBy`, `@LastModifiedDate`.
* **Mechanism:** The `@EntityListeners(AuditingEntityListener.class)` intercepts lifecycle events (`PrePersist`, `PreUpdate`) and populates audit fields before executing SQL inserts or updates.
* **Enabling:** Must declare `@EnableJpaAuditing` on a configuration class.

## B. Soft Deletes (`@SQLDelete` + `@Where`)
Soft deletion marks a record as deleted (e.g. `status = 'DELETED'`) rather than physically removing the row.

1. **Automation (`@SQLDelete`):** Overrides JPA delete calls to run custom SQL updates:
   ```java
   @SQLDelete(sql = "UPDATE orders SET status = 'DELETED' WHERE id = ?")
   ```
2. **Filtering (`@Where`):** Appends filter constraints to all SELECT queries dynamically:
   ```java
   @Where(clause = "status <> 'DELETED'")
   ```

## C. The `@Where` Limitation
The static `@Where` clause is applied globally across all select queries.
* **The Problem:** It is difficult to bypass on demand (e.g., if an administrator wants to view deleted orders).
* **Fix:** Use Hibernate **Filters (`@FilterDef` & `@Filter`)** instead. They can be dynamically enabled or disabled at runtime inside the database session:
  ```java
  session.enableFilter("deletedFilter").setParameter("isDeleted", false);
  ```

## D. Senior-Level Interview Q&A
### Q1: Do `@PrePersist` and `@PreUpdate` interceptors run during bulk JPQL update queries?
* **Answer:** **No.** Bulk HQL/JPQL update queries run directly on the database engine. Since the entities do not pass through the Persistence Context or Lifecycle Interceptors, JPA Auditing listeners are completely bypassed.

### Q2: How does `@Where` affect lazy collection fetching?
* **Answer:** It automatically filters child collections. If an `Order` has 10 items, but 2 are soft-deleted, calling `order.getItems()` will load only the 8 active items, appending the `@Where` clause to the SQL join.
