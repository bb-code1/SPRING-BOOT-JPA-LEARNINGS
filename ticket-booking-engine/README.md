# 🎫 High-Concurrency Ticket Booking Engine

A production-grade, high-performance ticket reservation and inventory microservice built to showcase advanced **Spring Data JPA** and **Hibernate** concurrency controls, locking strategies, dynamic specification querying, and performance tuning models.

---

## 🛠️ Advanced Architectural Features Implemented

1. **Concurrency Protection (Database Row Locks):**
   * Allocates seats using `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT FOR UPDATE`) on the `EventInventory` entity to prevent concurrent double-booking anomalies on high-demand event ticketing.
2. **Dynamic Search Criteria (JPA Specifications):**
   * Dynamic search queries on Concert Events using strongly-typed predicate compositions via the `EventSpecifications` builder.
3. **High-Performance Keyset Pagination:**
   * Keyset pagination range seeks (`WHERE id > :lastId`) implemented alongside standard offset pagination, demonstrating constant-time $O(1)$ response times for large database listings.
4. **Data Auditing & Lifecycle Listeners:**
   * Automated entity creations and last-modified metadata auditing using `AuditingEntityListener` linked to `BaseEntity`.
5. **Data Deletion Auditing (Soft Deletes):**
   * Automated soft deletes on `Booking` using `@SQLDelete(sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")` paired with a global active `@Where(clause = "status <> 'CANCELLED'")` filter.
   * Admin-level native queries bypass soft-delete active filters to show cancelled audit trails.
6. **Polymorphic Database Inheritance:**
   * Single-table entity inheritance hierarchy (`Payment` superclass linked to concrete subclasses `CreditCardPayment` and `PaypalPayment`).
7. **N+1 Query Prevention:**
   * Custom query bindings in `BookingRepository` using HQL `JOIN FETCH` to eagerly load child ticket items in a single SQL statement.
8. **Performance Configurations:**
   * Fixed HikariCP connection pools, Hibernate statement ordering (`order_inserts`, `order_updates`), and batch fetch sizes (`default_batch_fetch_size`).

---

## 🏃 Run & Verify

1. **Build & Package:**
   ```bash
   ./mvnw clean package
   ```
2. **Run Microservice:**
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Interactive UI Dashboard:**
   Open [http://localhost:8080/index.html](http://localhost:8080/index.html) in your browser to interact with the glassmorphism dark-mode simulation panel, trigger dynamic searches, and run live multi-threaded concurrent checkout simulations.
