# Day 22: Inheritance Mapping Strategies

## A. Inheritance Mapping Types
JPA supports mapping object-oriented class hierarchies to relational database tables using three strategies:

1. **`SINGLE_TABLE` (Default):**
   * Maps all classes in the hierarchy to a single database table.
   * Uses a **Discriminator Column** to differentiate subclass instances.
   * **Pros:** Fast queries (no joins or unions required).
   * **Cons:** All subclass-specific columns must be nullable (violates schema constraints).
2. **`JOINED`:**
   * Maps each class in the hierarchy to its own database table.
   * Subclass tables contain only subclass-specific columns and a primary key linking back to the parent table.
   * **Pros:** Highly normalized schema; database integrity constraints are preserved.
   * **Cons:** Queries require complex SQL joins (`JOIN`), degrading performance.
3. **`TABLE_PER_CLASS`:**
   * Maps each concrete class in the hierarchy to a separate database table containing all inherited columns.
   * **Pros:** Clean physical separation.
   * **Cons:** Polymorphic queries require database-intensive `UNION` statements.

## B. Architectural Trade-Offs
```
               SINGLE_TABLE                 JOINED
Schema:     [ Single Wide Table ]    [ Parent ] <-- [ Subclass ]
Queries:         Super Fast               Slow (Joins Required)
Constraints:  No NOT NULLs allowed      Strict NOT NULLs allowed
```

## C. Senior-Level Interview Q&A
### Q1: When do you choose `SINGLE_TABLE` over `JOINED` inheritance mapping?
* **Answer:** Use `SINGLE_TABLE` for high-performance polymorphic queries when subclass data structures are similar and have few subclass-specific fields. Use `JOINED` when data integrity is critical and database normalizations are required (allowing `NOT NULL` constraints on subclass columns).

### Q2: Why is `TABLE_PER_CLASS` generally avoided in production schemas?
* **Answer:** Polymorphic queries (e.g. `SELECT p FROM Payment p` where Payment is the base class) generate SQL queries with `UNION` blocks across all subclass tables. This forces database engine full tables scans and temp file creations, significantly degrading query speeds.
