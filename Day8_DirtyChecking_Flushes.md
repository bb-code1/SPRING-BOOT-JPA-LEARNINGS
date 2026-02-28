# Day 8: Dirty Checking, Snapshots, and Flushes

## A. Dirty Checking Internals
**Dirty Checking** is the process by which Hibernate automatically detects state changes in managed entities and syncs them to the database at commit time, eliminating the need to call `repository.save()`.

* **The Loaded State Snapshot:** When an entity is loaded into the Persistence Context, Hibernate clones its field values into a private array known as the **Loaded State Snapshot** (stored inside `EntityEntry.getLoadedState()`).
* **Comparison Loop:** During the flush phase, Hibernate loops through all managed entities and compares their current field values against the stored snapshot. If any value differs, the entity is marked "dirty."

## B. Flush Modes (`FlushModeType`)
A **Flush** converts in-memory entity changes into SQL statements (`INSERT`, `UPDATE`, `DELETE`) and sends them to the database transaction log. It does **not** commit the transaction.
* **`FlushModeType.AUTO` (Default):** Flushes occur before:
  1. Transaction commit.
  2. Any query execution (JPQL, Criteria, Native) whose results could be affected by pending updates.
* **`FlushModeType.COMMIT`:** Flushes occur **only** during transaction commit. Running a JPQL query mid-transaction will not flush changes, potentially returning stale database states.

## C. The Action Queue
Once dirty entities are identified, Hibernate builds SQL DML operations and puts them in the **`ActionQueue`**. The execution order of flushes is highly deterministic:
1. Orphan removals and collection updates.
2. Entity insertions.
3. Entity updates.
4. Collection deletions.
5. Entity deletions.

## D. Senior-Level Interview Q&A
### Q1: How does `@DynamicUpdate` affect dirty checking and performance?
* **Answer:** By default, Hibernate generates a static update SQL statement containing all columns. If only one column changes, it updates all columns. Annotating an entity with `@DynamicUpdate` instructs Hibernate to analyze the dirty check state and dynamically build an SQL statement containing **only the modified columns**.
* **Trade-off:** It saves network payload and reduces lock times on columns in some databases, but requires extra CPU overhead at runtime to dynamically construct SQL strings.

### Q2: Why is calling `save()` in a transactional service method an anti-pattern?
* **Answer:** For managed entities, calling `save()` or `saveAndFlush()` triggers redundant lookup operations (Hibernate checks if the entity is transient by checking its ID, potentially running extra SELECT queries or executing instant flushing before the transaction boundaries are set). It bypasses the benefit of Hibernate's transaction write-behind optimizations.
