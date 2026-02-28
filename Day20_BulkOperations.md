# Day 20: Bulk Operations & Persistence Context Sync

## A. Bulk Operations vs. Entity Lifecycle
Bulk operations (using JPQL/HQL `UPDATE` or `DELETE` statements) execute directly on the database engine, bypassing Hibernate's entity lifecycle mechanics.
* **The Pitfall:** Because the queries bypass the Persistence Context, Hibernate does not know that the database state of managed entities has changed. This leads to **Persistence Context Desync** (stale data in L1 Cache).

## B. The Stale Cache Hazard
1. Entity A (status = "ACTIVE") is loaded into the L1 Cache.
2. A bulk operation is executed: `UPDATE User u SET u.status = 'INACTIVE' WHERE u.id = A.id`.
3. The database updates the row.
4. Calling `A.getStatus()` still returns `"ACTIVE"`!
5. When the transaction commits, Hibernate flushes Entity A's snapshot, updating the database status back to `"ACTIVE"`, silently reverting the bulk update!

## C. Solution: `@Modifying` Configuration
Use the `@Modifying` annotation configuration properties in Spring Data JPA:
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
int updateStatusBulk(Long id, String status);
```
* **`flushAutomatically = true`:** Flushes any pending entity modifications in the session to the database before the bulk query runs, preventing modifications collision.
* **`clearAutomatically = true`:** Automatically detaches all entities from the L1 Cache after the bulk query completes, forcing subsequent queries to load fresh data from the database.

## D. Senior-Level Interview Q&A
### Q1: What is the risk of using `@Modifying(clearAutomatically = false)`?
* **Answer:** Stale entities remain in the L1 Cache. If those entities are modified and flushed later in the same transaction, they will overwrite the database modifications performed by the bulk query with their stale cache states.

### Q2: Why must bulk delete queries be handled with care when using cascading rules?
* **Answer:** Bulk SQL delete queries bypass JPA cascade rules (e.g. `CascadeType.REMOVE`). If you delete a parent entity via a bulk query, Hibernate will not delete the associated children, resulting in database foreign key constraint violations or orphaned records.
