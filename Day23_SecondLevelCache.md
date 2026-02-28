# Day 23: Second-Level Cache Architecture

## A. Cache Hierarchy: L1 vs L2
Hibernate implements a two-level caching architecture to minimize query latency and database read execution:

* **First-Level (L1) Cache:** Thread-local, transaction-scoped, active by default. Not shared.
* **Second-Level (L2) Cache:** JVM-scoped or clustered (e.g. using Ehcache, Hazelcast, or Redis), shared across database sessions. Off by default.

```
[Session 1] ---> [L1 Cache (Local)] ----+
                                        |
[Session 2] ---> [L1 Cache (Local)] ----+---> [L2 Cache (JVM Shared)] ---> [Database]
```

## B. L2 Cache Concurrency Strategies
You must define how the L2 cache handles concurrent read and write operations:
1. **`READ_ONLY`:** Safe for static lookup tables. Throws an exception if updates are attempted.
2. **`NONSTRICT_READ_WRITE`:** Weak consistency. Stale cache reads are possible before expiration.
3. **`READ_WRITE`:** Strong consistency. Uses soft locks during updates to prevent stale reads.
4. **`TRANSACTIONAL`:** JTA-transactional cache. Guarantees ACID compliance.

## C. Entity Caching vs Query Caching
* **Entity Caching:** Stores raw entity data indexed by ID. It has a high hit rate.
* **Query Caching:** Stores query results (lists of entity IDs) indexed by the query string and its parameters. It is highly volatile: **any modification to the target table invalidates all cached query results for that table**.

## D. Senior-Level Interview Q&A
### Q1: What is the main danger of using L2 Query Caching without Entity Caching?
* **Answer:** **The N+1 Cache Miss Trap.** If the query cache is hit, it retrieves a list of entity IDs. If those entity IDs are not cached in the L2 Entity Cache, Hibernate is forced to execute separate SQL SELECT queries to load each entity by its ID, generating N+1 queries.

### Q2: How does L2 Cache handle entity updates?
* **Answer:** For `READ_WRITE` strategy, during an update:
  1. The transaction updates the database and places a soft lock on the cache entry.
  2. Other transactions bypass the cache and read from the database directly while the lock is held.
  3. When the transaction commits, the lock is released, and the new state is cached.
