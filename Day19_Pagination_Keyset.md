# Day 19: Offset Pagination vs. Keyset Pagination

## A. Offset Pagination Mechanics
**Offset Pagination** is the standard approach to page through datasets in JPA (using `Pageable` and `PageRequest`).
* **Under the Hood SQL:**
  ```sql
  SELECT * FROM orders ORDER BY order_date DESC LIMIT 20 OFFSET 100000;
  ```
* **The Performance Bottleneck:** The database cannot jump directly to row 100,000. It must read all 100,020 rows from the index or disk, sort them, and then discard the first 100,000 rows. As the page index increases, execution latency increases proportionally.

## B. Keyset Pagination (Seek Method)
**Keyset Pagination** uses the values of the last record returned in the previous page as a filter constraint for the next query.
* **Under the Hood SQL:**
  ```sql
  SELECT * FROM orders WHERE id > :last_seen_id ORDER BY id ASC LIMIT 20;
  ```
* **Performance Benefit:** This performs an **Index Range Scan** (O(1) constant seek speed), completely avoiding scanning or sorting preceding rows.

## C. Comparison Summary
| Feature | Offset Pagination | Keyset Pagination |
| :--- | :--- | :--- |
| **Response Latency** | Degrades as page count grows | Constant (O(1) seek times) |
| **Random Page Access**| Yes (can jump to page 10) | No (must page sequentially) |
| **Real-time Drift**  | Prone to skipping/duplicating rows| Stable (immune to inserts/deletes)|
| **Query Complexity** | Highly standard | Requires complex filtering |

## D. Senior-Level Interview Q&A
### Q1: Why does offset pagination degrade under high offset numbers?
* **Answer:** Because the database engine must scan and load all rows matching the sort criteria up to the offset count before discarding them. For wide tables, this triggers massive disk reads and high temp file sorting memory allocations in database engines.

### Q2: How do you implement Keyset Pagination in Spring Data JPA?
* **Answer:** Use custom Specifications or query parameters:
  ```java
  public interface OrderRepository extends JpaRepository<Order, Long> {
      @Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id ASC")
      List<Order> findNextPage(Long lastId, Pageable pageable);
  }
  ```
