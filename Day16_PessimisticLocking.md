# Day 16: Pessimistic Locking (`SELECT FOR UPDATE`)

## A. Pessimistic Locking Modes
**Pessimistic Locking** is a concurrency control strategy that assumes conflicts are frequent. It acquires physical database-level row locks immediately during query execution, blocking other transactions.

1. **`PESSIMISTIC_READ` (Shared Lock / Read Lock):**
   * Acquires a shared lock (`SELECT FOR SHARE` in PostgreSQL).
   * Allows other transactions to read the rows, but blocks them from writing or acquiring exclusive locks.
2. **`PESSIMISTIC_WRITE` (Exclusive Lock / Write Lock):**
   * Acquires an exclusive lock (`SELECT FOR UPDATE` in PostgreSQL/Oracle).
   * Blocks other transactions from reading (if using SELECT FOR UPDATE), writing, or acquiring any locks.

## B. JPA `@Lock` Declarations
Declare locks on Spring Data JPA repository methods:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.id = :id")
Optional<Order> findByIdForUpdate(Long id);
```

## C. Lock Scopes & Timeouts
* **Lock Timeout Hint:** To prevent blocking indefinitely, set a query timeout hint:
  ```java
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
  ```
  If the lock cannot be acquired within 5000ms, the query throws a `PessimisticLockException`.

## D. Senior-Level Interview Q&A
### Q1: When do you choose Optimistic Locking over Pessimistic Locking?
* **Answer:**
  * **Optimistic Locking:** Preferred in high-read, low-write collision environments. It avoids locking overhead and maximizes database connection utilization, but conflicts must be handled at the application layer.
  * **Pessimistic Locking:** Preferred in high-collision transactional environments (e.g., inventory seat booking, financial ledger accounts) where concurrency conflicts are common. It guarantees immediate consistency at the cost of database throughput.

### Q2: What is the cause of Deadlocks in Pessimistic locking, and how do you prevent them?
* **Answer:** A **Deadlock** occurs when Transaction 1 holds a lock on Row A and waits for Row B, while Transaction 2 holds a lock on Row B and waits for Row A.
* **Prevention:**
  1. Always acquire locks in a consistent alphabetical or numerical order (e.g., sort ID lists before locking).
  2. Keep transactions as short as possible (avoid network calls or slow operations mid-lock).
  3. Always configure lock timeouts.
