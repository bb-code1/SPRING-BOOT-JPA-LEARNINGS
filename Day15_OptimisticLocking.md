# Day 15: Optimistic Locking (`@Version`)

## A. JPA Optimistic Locking Mechanics
**Optimistic Locking** is a concurrency control strategy that assumes conflicts are rare. It prevents "lost updates" without holding lock states in database memory.
* **The `@Version` Attribute:** An entity is mapped with a version field (usually an `Integer` or `Long` annotated with `@Version`):
  ```java
  @Version
  private Integer version;
  ```
* **SQL Mechanics:** Whenever Hibernate updates a versioned entity, it appends the version check to the SQL query and increments it:
  ```sql
  UPDATE orders SET status = ?, version = version + 1 WHERE id = ? AND version = ?;
  ```

## B. Handling Version Conflicts
1. Thread A loads Order 1 (version = 1).
2. Thread B loads Order 1 (version = 1).
3. Thread A updates status and commits. Database increments version to 2.
4. Thread B attempts to update. Its statement is executed with `WHERE id = 1 AND version = 1`.
5. Because the version is now 2, the query matches **0 rows**.
6. Hibernate detects that 0 rows were updated and throws **`OptimisticLockException`** (Spring wraps this as `ObjectOptimisticLockingFailureException`), causing Thread B's transaction to roll back.

```
Thread A: [Read version=1] ----------> [Commit version=2]
Thread B:        [Read version=1] ------------------------> [Commit failed (version is 2)]
```

## C. Versionless Optimistic Locking
If you cannot modify the database schema to add a version column, Hibernate supports versionless optimistic locking:
* Annotate the entity with `@OptimisticLocking(type = OptimisticLockType.ALL)` or `DIRTY`.
* Hibernate will use all columns (or dirty columns only) in the update statement's `WHERE` clause:
  ```sql
  UPDATE orders SET status = ? WHERE id = ? AND status = ? AND order_date = ?;
  ```

## D. Senior-Level Interview Q&A
### Q1: What happens to the version field if only a child collection is modified?
* **Answer:** By default, modifying a child collection (e.g. adding an item to `Order.items`) **does not** increment the parent's (`Order`) version field.
* **Fix:** Use `LockModeType.OPTIMISTIC_FORCE_INCREMENT` when fetching the parent entity to force a version increment on save, indicating the parent aggregate root state has changed.

### Q2: How do you handle `OptimisticLockingFailureException` in high-throughput APIs?
* **Answer:** High-concurrency conflict points should implement a **Retry Aspect** using Spring Retry. It catches the exception and retries the transaction (e.g. up to 3 times with a backoff delay) to load the latest snapshot and reapply mutations.
