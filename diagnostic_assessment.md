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
