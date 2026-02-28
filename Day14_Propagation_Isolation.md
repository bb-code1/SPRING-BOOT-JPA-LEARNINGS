# Day 14: Transaction Isolation & Propagation

## A. Transaction Propagation Settings
Propagation defines what happens to a transaction boundary when a method is called by a client that is already running in a transactional context.

* **`REQUIRED` (Default):** Runs inside the existing transaction if one exists. If not, it creates a new one.
* **`REQUIRES_NEW`:** Suspends the existing transaction, opens a new database connection, and runs inside a completely new transaction context. When the inner transaction finishes, the suspended outer transaction resumes.
* **`NESTED`:** Runs inside a nested transaction using database **Savepoints**. If the nested transaction fails, it rolls back to the savepoint without rolling back the outer transaction. (Not supported by all JPA/Hibernate dialects).
* **`MANDATORY`:** Requires an existing transaction. Throws an exception if none is active.

## B. Transaction Isolation Levels
Isolation defines the degree to which transaction state modifications are visible to other concurrent transactions.

1. **`READ_UNCOMMITTED`:** Allows dirty reads (transactions read uncommitted modifications from other threads).
2. **`READ_COMMITTED` (PostgreSQL/Oracle Default):** Prevents dirty reads. Prevents reading uncommitted data.
3. **`REPEATABLE_READ`:** Prevents non-repeatable reads (reading the same row twice inside a transaction guarantees returning identical states, even if other threads committed changes).
4. **`SERIALIZABLE`:** Full execution isolation. Prevents phantom reads. High database locking overhead.

## C. Isolation Anomalies
| Isolation Level | Dirty Reads | Non-Repeatable Reads | Phantom Reads |
| :--- | :--- | :---: | :---: |
| **`READ_UNCOMMITTED`**| Allowed | Allowed | Allowed |
| **`READ_COMMITTED`**  | Prevented | Allowed | Allowed |
| **`REPEATABLE_READ`**  | Prevented | Prevented | Allowed (DB specific)|
| **`SERIALIZABLE`**     | Prevented | Prevented | Prevented |

## D. Senior-Level Interview Q&A
### Q1: If an inner method annotated with `REQUIRES_NEW` rolls back, does the outer transaction roll back?
* **Answer:** **Yes, if the exception propagates up.** If the inner method throws an exception, the inner transaction rolls back. If the outer method does not catch this exception, it propagates to the outer proxy interceptor, triggering a rollback of the outer transaction too.
* **Fix:** To prevent outer rollback, wrap the inner call in a `try-catch` block in the outer method.

### Q2: What is the performance impact of using `SERIALIZABLE` isolation in a high-concurrency system?
* **Answer:** It degrades performance significantly because it forces range locks and lock escalations on tables. Concurrent readers and writers block each other, causing timeouts and frequent **Deadlocks** or serialization failures, reducing database throughput.
