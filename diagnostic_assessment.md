# Diagnostic Assessment

This document tracks the 10 diagnostic questions used to assess baseline knowledge before beginning the advanced curriculum.

---

## Diagnostic Question #1: Entity Lifecycle & Persistence Context

**Question:**
Suppose you have a standard Spring Boot service method annotated with `@Transactional`:

```java
@Transactional
public void updateUserName(Long userId, String newName) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    user.setName(newName);
}
```

Notice that we **never** call `userRepository.save(user)` or `entityManager.merge(user)`. 

1. **Will this modification be saved to the database when the method exits?**
2. **Explain the complete internal mechanics** of what happens inside Spring, JPA, Hibernate, and the database from the moment `user.setName(newName)` is called to the moment the transaction commits.

**Your Answer:**
1. **Yes, the modification will be saved.** There is no need to call `save()` because the entity is in a `MANAGED` state.
2. **Internal Mechanics:**
   * **Transaction Start:** Spring's transaction interceptor starts a transaction via the `PlatformTransactionManager`, binds a JDBC Connection to the thread, opens a Hibernate `Session`, and sets `connection.setAutoCommit(false)`.
   * **Entity Loading:** `findById()` executes a SELECT query. When the row is loaded, Hibernate instantiates the `User` object, stores it in the **First-Level Cache (Persistence Context)**, and takes an internal **Snapshot** copy of its state.
   * **Mutation:** Calling `user.setName(newName)` modifies the object in JVM heap memory. No SQL is executed yet.
   * **Method Exit & Commit:** When the method exits, Spring triggers a transaction commit.
   * **Session Flush:** Before committing, the Session flush is triggered. Hibernate performs **Dirty Checking** by comparing the current entity properties against the cached **Snapshot**.
   * **DML Execution:** Since it detects a difference, Hibernate generates and executes an `UPDATE` SQL statement via JDBC.
   * **Commit:** `connection.commit()` is executed on the database, persisting the changes and releasing row locks.

---

## Diagnostic Question #2: Lazy Loading & Proxy Objects

**Question:**
Suppose you have an `Order` entity containing a list of `OrderItem` objects, mapped as a lazy-loaded collection:

```java
@Entity
public class Order {
    @Id @GeneratedValue
    private Long id;
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items;
}
```

If you fetch this `Order` within a service layer transaction, and then pass it to a REST Controller where (outside the transaction) you call `order.getItems().size()`:

1. **What exception is thrown?**
2. **Explain the internal implementation of a Hibernate Proxy**—how does Hibernate dynamically intercept getter calls?
3. **What are three production-ready solutions** to resolve this issue, and what are the trade-offs of each?

**Your Answer:**
1. **Exception:** `org.hibernate.LazyInitializationException` is thrown.
2. **Internal Proxy Implementation:**
   * **Dynamic Subclassing:** At runtime, Hibernate uses **Byte Buddy** to generate a dynamic proxy class extending your entity (e.g., `Order$HibernateProxy`).
   * **Interceptor:** The proxy overrides all non-final methods. It contains a `LazyInitializer` field which holds the entity ID and a reference to the active `Session`.
   * **Crash Scenario:** When you invoke `getItems()`, the proxy attempts to initialize the collection. It checks its `Session` reference. Because the transaction has ended, the Session is **closed**. Without a database connection, it throws the exception.
3. **Solutions & Trade-offs:**
   * **A. JOIN FETCH HQL Query:** Fetches parent and children in one database query. *Trade-off:* High performance, but can trigger Cartesian products if joining multiple collection associations.
   * **B. Spring `@EntityGraph`:** Declaratively defines fetch plans. *Trade-off:* Elegant metadata, but lacks query flexibility.
   * **C. DTO Projections:** Selects only specific columns directly into flat records. *Trade-off:* Absolute best performance and memory usage, but results are read-only.

---

## Diagnostic Question #3: The `@Transactional` Proxy & Self-Invocation

**Question:**
Suppose you have the following Spring Boot service class:

```java
@Service
public class OrderService {

    public void processOrder(Long orderId) {
        // Business logic...
        saveOrderLog(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderLog(Long orderId) {
        // SQL updates...
    }
}
```

If we call `processOrder(orderId)` from a REST Controller:
1. **Will `saveOrderLog` execute inside a transaction?** Explain why or why not.
2. **Explain the internal Spring Proxy mechanism**—how does dynamic proxying or CGLIB intercept `@Transactional` annotations?
3. **What is the self-invocation issue**, and what are two ways to resolve it?

**Your Answer:**
1. **No transaction will run.** `saveOrderLog` will run as a plain Java method call without transaction boundaries.
2. **Spring Proxy Mechanism:**
   * When Spring bootstraps a bean with `@Transactional`, it wraps the target bean in a **Proxy Bean** (usually CGLIB subclass proxy).
   * External callers (like the REST Controller) call methods on the **Proxy**. The Proxy starts the transaction, delegates to the actual target bean, and commits/rolls back.
3. **Self-Invocation:**
   * When `processOrder` calls `saveOrderLog` internally, it references `this.saveOrderLog(...)` directly on the target instance, completely bypassing the Spring Proxy wrap.
   * **Resolutions:**
     * *A. Self-Injection:* Autowire the service bean into itself (lazy-loaded proxy reference) and call `self.saveOrderLog(...)`.
     * *B. Helper Class:* Move the transactional method to a separate helper service bean.
     * *C. Programmatic Transactions:* Use `TransactionTemplate` manually.

---

## Diagnostic Question #4: N+1 Select Problem & Batch Fetching

**Question:**
You load 100 `Order` entities from the database. When your code iterates through them and calls `order.getItems().size()`, it triggers 100 additional SELECT queries.
1. **Why does this happen?**
2. **Explain how `@BatchSize` or `hibernate.default_batch_fetch_size` resolves this internally.** What SQL is generated?

**Your Answer:**
1. **N+1 Selection:** The initial query loads 100 orders (1 query). When accessing the lazy collection for each order, Hibernate executes a query per order to load its items (N queries). Total: 1 + 100 = 101 queries.
2. **Batch Fetching Mechanics:**
   * By setting `@BatchSize(size = 20)` on the collection or globally using `hibernate.default_batch_fetch_size=20`, you instruct Hibernate to batch-load uninitialized proxies.
   * When you access the first order's items, Hibernate scans the active session for other uninitialized order collections of the same type.
   * It aggregates their parent IDs (up to size 20) and executes a single query using an `IN` clause:
     ```sql
     SELECT * FROM order_items WHERE order_id IN (?, ?, ?, ?, ... ?);
     ```
   * This reduces database round-trips from 101 queries to 6 queries (1 initial + 5 batch fetches).

---

## Diagnostic Question #5: MultipleBagFetchException & Cartesian Products

**Question:**
Why does Hibernate throw `org.hibernate.loader.MultipleBagFetchException` when you try to `JOIN FETCH` two collection associations (e.g., `Order` -> `items` and `Order` -> `promotions`) in the same HQL query? How do you resolve this?

**Your Answer:**
1. **Why it occurs:** A "Bag" in Hibernate represents an unordered collection that allows duplicate elements (mapped to a Java `List`). If you perform two outer joins on a single parent (Orders), the database returns a **Cartesian Product** (multiplying items x promotions). Hibernate cannot figure out how to separate the duplicate records into the respective Lists without creating duplicate entities in memory.
2. **Resolution:**
   * **Best Practice:** Split the retrieval. Fetch the parent and the first collection with a join fetch, and let Hibernate fetch the second collection using Batch Fetching (`@BatchSize`).
   * **Alternative:** Change collection types from `List` to `Set`. However, this is a performance trap because it hides the Cartesian product under the hood, fetching a huge duplicate result set over the network and sorting it in JVM memory.

---

## Diagnostic Question #6: Pagination with JOIN FETCH warning

**Question:**
Why does Hibernate output: `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!` when you combine a pagination query (`Pageable`) with a `JOIN FETCH` on a collection? What is the danger?

**Your Answer:**
1. **Why it occurs:** When joining parent and child rows, the SQL database returns multiple rows for a single parent (e.g. 1 order with 5 items returns 5 database rows). If you ask for `LIMIT 10`, the database limits to 10 *join rows*, which would cut off the items of the last order. To prevent partial entity state loading, Hibernate fetches **every single row** from the database, loads them into memory, and performs the page offset filtering inside JVM memory.
2. **Danger:** In production, if the table contains 10 million rows, Hibernate will attempt to load all 10 million rows into the JVM heap memory to return page 1. This causes massive garbage collection spikes, high CPU usage, and inevitably throws an **OutOfMemoryError (OOM)**.
3. **Resolution:** Remove `JOIN FETCH` from pagination queries. Use `@BatchSize` or run a two-step retrieval (fetch parent IDs first, then fetch their collections).

---

## Diagnostic Question #7: Optimistic Locking & Version Checking

**Question:**
How does `@Version` optimistic locking prevent the "lost update" problem? Explain the SQL executed by Hibernate when an entity is updated.

**Your Answer:**
1. **The Lost Update:** Occurs when two concurrent transactions read the same data, modify it, and commit. Transaction B overwrites the changes made by Transaction A without realizing it.
2. **Optimistic Locking:**
   * You add a `@Version` field (usually integer or timestamp) to the entity.
   * When Hibernate executes the update, it appends the version check to the `WHERE` clause and increments the version:
     ```sql
     UPDATE orders SET total_amount = ?, version = version + 1 
     WHERE id = ? AND version = ?;
     ```
   * If Transaction B tries to commit after Transaction A, the version in the database has already incremented to `2`. Transaction B's update statement matches **0 rows**.
   * Hibernate checks the update row count returned by JDBC. Since it is `0`, Hibernate throws `OptimisticLockException` and rolls back Transaction B, protecting data integrity.

---

## Diagnostic Question #8: Bulk Operations & Persistence Context Desync

**Question:**
You execute a bulk update:
```java
@Modifying
@Query("UPDATE User u SET u.status = 'ACTIVE'")
void activateAllUsers();
```
If you query users using `userRepository.findById()` in the same transaction *after* running this bulk update, why do you still see the old status values? How do you fix this?

**Your Answer:**
1. **Why it occurs:** Bulk queries run directly in the database, completely bypassing the Hibernate Persistence Context (First-Level Cache). Any entities that were loaded into the L1 cache before the bulk update remain in memory with their old field values. Hibernate's `findById()` reads from the L1 cache first, returning stale data.
2. **Resolution:** Annotate the repository method with `@Modifying(clearAutomatically = true)`:
   ```java
   @Modifying(clearAutomatically = true)
   @Query("UPDATE User u SET u.status = 'ACTIVE'")
   void activateAllUsers();
   ```
   This clears the persistence context immediately after the DML statement, forcing subsequent queries to read the updated values directly from the database.

---

## Diagnostic Question #9: First-Level Cache (L1) vs. Second-Level Cache (L2)

**Question:**
Compare the L1 and L2 caches in Hibernate. What are their scope boundaries, thread-safety characteristics, and lifecycles?

**Your Answer:**
* **First-Level Cache (L1):**
  * **Scope:** Session-scoped. Bound to the active transaction thread.
  * **Thread Safety:** Not thread-safe. Only accessed by the single thread executing the Session transaction.
  * **Lifecycle:** Cleared and closed automatically when the transaction finishes or Session closes. It is enabled by default and cannot be disabled.
* **Second-Level Cache (L2):**
  * **Scope:** SessionFactory-scoped. Application-wide.
  * **Thread Safety:** Thread-safe. Accessed by concurrent transaction threads.
  * **Lifecycle:** Lives as long as the application is running. It stores data across transaction boundaries, handles clustered synchronization (via providers like Redis, Ehcache, or Hazelcast), and requires explicit configuration to activate.

---

## Diagnostic Question #10: Saving Transient Entities & Identity Key Overhead

**Question:**
Why does Hibernate disable JDBC batch inserts when using `GenerationType.IDENTITY` for primary key generation? What is the performance impact?

**Your Answer:**
1. **Why it occurs:** In Hibernate, every entity in the `MANAGED` state must have a unique identifier (Primary Key) to map it in the L1 cache. With `GenerationType.IDENTITY`, the ID is generated by the database auto-increment column *during* row insertion.
2. **Overhead:** Hibernate cannot delay the INSERT execution because it needs the ID. It must execute the INSERT statement immediately, retrieve the generated key using JDBC `getGeneratedKeys()`, and associate it with the entity. This forces row-by-row inserts and disables **JDBC Batching** (which requires grouping inserts together and executing them in a single batch).
3. **Performance Impact:** High database round-trip latency. For batch loads, `GenerationType.SEQUENCE` is preferred because Hibernate can pre-fetch sequence ranges in a single call, allowing it to batch DML statements efficiently.
