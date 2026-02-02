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
1. **Yes, the modification will be saved to the database.** There is no need to call `save()` or `merge()` because the entity is in a `MANAGED` state within the active Hibernate Session.

2. **Internal Mechanics (Step-by-Step):**

```text
Spring Transaction Interceptor
     ↓ (Intercepts method call, gets Connection from HikariCP)
Database Transaction Started (connection.setAutoCommit(false))
     ↓
Hibernate Session Opened & Bound to Thread (Persistence Context initialized)
     ↓
userRepository.findById(userId) executes SELECT query
     ↓
Entity Loaded into memory (User instance created)
     ↓
JPA MANAGED State & Snapshot Created (Stored in First-Level Cache / Persistence Context)
     ↓
user.setName(newName) modifies Java heap memory
     ↓ (No SQL generated yet)
Service Method Exits Successfully
     ↓
Spring Transaction Interceptor initiates Commit
     ↓
Hibernate Session FLUSH triggered (Dirty Checking)
     ↓ (Compares current User fields against cached original Snapshot)
Action Queue generates SQL UPDATE statement
     ↓
JDBC sends UPDATE users SET name = ? WHERE id = ? to DB
     ↓
Database runs UPDATE (locks row)
     ↓
connection.commit() called on JDBC Connection
     ↓ (Locks released, data written to transaction logs)
Hibernate Session Closed / Persistence Context Cleared (Entity becomes DETACHED)
```

### Detailed Internals:
* **The Snapshot Cache:** When Hibernate retrieves the `User` entity from the database, it creates a duplicate copy of the column values in a private array called the **Snapshot**.
* **Dirty Checking:** During the flush phase (right before database commit), Hibernate loops through all entities in the first-level cache and compares their current properties with their corresponding snapshot values. If it detects a mismatch, it marks the entity as **dirty** and queues the appropriate DML update in the `ActionQueue`.
* **Database Participation:** The database participates by locking the target row during the `UPDATE` statement execution. The change is made permanent only when `connection.commit()` is successfully received.

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
    
    // Getters and setters...
}
```

If you fetch this `Order` within a service layer transaction, and then pass it to a REST Controller where (outside the transaction) you call `order.getItems().size()`:

1. **What exception is thrown?**
2. **Explain the internal implementation of a Hibernate Proxy**—how does Hibernate dynamically intercept getter calls?
3. **What are three production-ready solutions** to resolve this issue, and what are the trade-offs of each?

**Your Answer:**
1. **Exception Thrown:** `org.hibernate.LazyInitializationException` is thrown.

2. **Internal Proxy Implementation:**
   * **Subclassing:** At startup or dynamic loading, Hibernate uses **Byte Buddy** (or Javassist in older versions) to generate a dynamic subclass of the entity at runtime (e.g., `Order$HibernateProxy$3a8x`).
   * **LazyInitializer Interceptor:** This dynamic proxy implements the `HibernateProxy` interface and overrides all non-final getters and setters. It holds an internal `LazyInitializer` field containing the database primary key ID and a reference to the active Hibernate `Session`.
   * **Interception:** When `order.getItems()` is called, the proxy intercepts the call. If the target collection is uninitialized, the proxy checks its `Session` reference. Because the transaction has ended, the Hibernate Session is **closed**. The proxy cannot access the database to load the collection, throwing the `LazyInitializationException`.

3. **Three Production-Ready Solutions & Trade-offs:**
   * **Solution A: JOIN FETCH Queries**
     * *Implementation:* Write a custom repository query: `SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id`.
     * *Trade-off:* Highly performant because it retrieves the order and its items in a single database round-trip using an SQL join. However, you cannot fetch multiple collections simultaneously (`MultipleBagFetchException` is thrown to prevent massive memory Cartesian products).
   * **Solution B: Spring Data JPA Entity Graph (`@EntityGraph`)**
     * *Implementation:* Annotate repository method with `@EntityGraph(attributePaths = {"items"})`.
     * *Trade-off:* Clear declarativeness, allowing you to fetch relationships dynamically without rewriting JPQL. But it has similar limitations regarding Cartesian products and makes query customization less flexible.
   * **Solution C: DTO Projections**
     * *Implementation:* Query only the necessary fields directly into a custom DTO class or record.
     * *Trade-off:* Outstanding performance. It fetches only the required database columns and bypasses Hibernate entity management entirely, meaning zero memory cache dirty checks. However, the result is read-only.
