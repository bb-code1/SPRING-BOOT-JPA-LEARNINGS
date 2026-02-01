# Advanced Spring Data JPA & Hibernate Learning Roadmap

Welcome to the **Spring Data JPA & Hibernate Expert Development Portal**. This repository documents an advanced, production-oriented curriculum designed to transition your skills from intermediate database access to Senior/Staff Java Developer patterns.

---

## 📅 Curriculum Roadmap (Feb 1 - Feb 28, 2026)

### 🚀 Phase 1: Diagnostic Assessment & Foundations
* **Day 1: Roadmap Initialization & Diagnostic Phase**
  * Establish the diagnostic assessment.
  * Establish core ORM mappings, entity states, and identifier generation strategies.
* **Day 2: Java Persistence API (JPA) vs. Hibernate Architecture**
  * Bootstrap process, SessionFactory, Entity Managers, and native Hibernate integrations.
* **Day 3: Primary Key Generation Strategies & Embeddables**
  * Performance profiles of `IDENTITY`, `SEQUENCE`, `TABLE`, and dynamic `UUID` generators.

### 🔗 Phase 2: High-Concurrency & Bidirectional Mappings
* **Day 4: Bidirectional Relationship Mechanics**
  * Designing `@OneToOne` and `@ManyToOne` boundaries.
* **Day 5: The `@OneToMany` & `@ManyToMany` Pitfalls**
  * Why blindly using `@ManyToMany` creates scale challenges and how to map intermediate entity tables.
* **Day 6: Entity Cascading and Orphan Removal**
  * Cascade styles, orphan removals, and serialization loops.

### 🧠 Phase 3: Hibernate Core Internals & Lifecycle
* **Day 7: The Persistence Context & First-Level Cache**
  * Session boundaries, transaction synchronizations, and internal maps.
* **Day 8: Dirty Checking, Snapshots, and Flushes**
  * Mutation detection, action queue execution, and flush strategy overrides.
* **Day 9: Proxy Objects & LazyInitializationException**
  * How Bytecode Enhancement and runtime proxies intercept getter calls.

### 🏎️ Phase 4: Fetching Strategies & Query Optimization
* **Day 10: The N+1 SELECT Query Problem**
  * Root cause analysis and detection hooks.
* **Day 11: JOIN FETCH vs. Entity Graph**
  * Solving relationships loading efficiently without causing Cartesian products.
* **Day 12: Batch Fetching & Projections**
  * `@BatchSize`, DTO Constructor Expressions, and Read-Only Query optimizations.

### 🔒 Phase 5: Transactions, Concurrency & Locking
* **Day 13: Declarative Transactions (`@Transactional`)**
  * Proxies, self-invocation pitfalls, and exception rollbacks.
* **Day 14: Transaction Isolation & Propagation**
  * Propagation boundary states (`REQUIRED`, `REQUIRES_NEW`) and read-only flags.
* **Day 15: Optimistic Locking (`@Version`)**
  * Lost updates protection and automatic version checks.
* **Day 16: Pessimistic Locking & Deadlock Handling**
  * `SELECT FOR UPDATE` mechanics and sorted locking structures.

### 🔍 Phase 6: Advanced Query Abstractions
* **Day 17: Query Specifications & Criteria API**
  * Dynamic, type-safe filtering schemas.
* **Day 18: Native Queries & Interface Projections**
  * High-performance database native calls and read-only models.
* **Day 19: Offset Pagination vs. Keyset Pagination**
  * Why `LIMIT/OFFSET` scales poorly and how cursor-based keysets resolve it.

### 📦 Phase 7: Bulk Operations & Advanced Mapping
* **Day 20: Bulk Operations & Persistence Context Sync**
  * Handling `@Modifying` data updates safely without leaving context stale.
* **Day 21: Composite Keys & Embeddables**
  * `@EmbeddedId` and `@IdClass` implementation strategies.
* **Day 22: Inheritance Mapping Strategies**
  * Performance, tables count, and query cost comparison: `SINGLE_TABLE`, `JOINED`, `TABLE_PER_CLASS`.

### 💾 Phase 8: Caching & System Architecture
* **Day 23: Second-Level Cache Architecture**
  * Integration with Ehcache/Redis, cache regions, and read/write strategies.
* **Day 24: Auditing & Soft Deletes**
  * Tracking mutations automatically and implementing logical deletes safely.
* **Day 25: Connection Pool Tuning (HikariCP)**
  * Maximum pool size sizing, leak detection, and wait timeouts.
* **Day 26: JDBC Batching & High-Volume Inserts**
  * Configuring rewriting batch updates to minimize database round trips.
* **Day 27: Production Troubleshooting & Slow Queries**
  * Logging SQL bindings, analyzing thread dumps, and locating database deadlocks.
* **Day 28: Final Architecture Summary & Testcontainers Verification**
  * Writing integration tests against Docker container database engines.

---

## 📝 Diagnostic Assessment
Before deep-diving into the daily code assets, we establish a **10-Question Diagnostic Assessment** to establish your baseline understanding of JPA and Hibernate internals.
