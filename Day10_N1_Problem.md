# Day 10: The N+1 SELECT Query Problem

## A. Definition & Root Cause
The **N+1 SELECT Query Problem** occurs when an application executes 1 query to fetch a list of `N` parent records, and then executes `N` additional queries to fetch associated child records for each parent.
* **Trigger:** This happens when code iterates over a collection of fetched parents and accesses a lazy-loaded collection or property on each instance:
  ```java
  List<Order> orders = orderRepository.findAll(); // 1 Query
  for (Order order : orders) {
      System.out.println(order.getItems().size()); // N Queries (1 per order)
  }
  ```

## B. The JPA FetchType.EAGER Trap
Many developers believe changing `FetchType.LAZY` to `FetchType.EAGER` solves the N+1 problem.
* **Reality:** It actually **causes** N+1 queries automatically!
* **Reasoning:** Eager loading tells JPA to make sure child records are present whenever the parent is loaded. When executing a JPQL query like `SELECT o FROM Order o`, Hibernate fetches the orders first. Because items are marked `EAGER`, it immediately runs separate SELECT queries to fetch items for each order, causing N+1 queries.

## C. How FetchMode.SUBSELECT Solves It
By mapping the association with `@Fetch(FetchMode.SUBSELECT)`:
1. The parent records are loaded in the first query.
2. The moment any child collection is accessed, Hibernate executes **exactly 1 additional query** retrieving children for **all** loaded parents via an SQL subquery:
   ```sql
   SELECT * FROM order_items WHERE order_id IN (SELECT id FROM orders);
   ```

## D. Senior-Level Interview Q&A
### Q1: What is the differences between `@Fetch(FetchMode.JOIN)` and JPQL `JOIN FETCH`?
* **Answer:** 
  * **`FetchMode.JOIN`** is a Hibernate-specific metadata annotation. It is only respected when loading an entity by its ID (e.g. `entityManager.find()`). It is ignored for JPQL queries.
  * **`JOIN FETCH`** is an HQL/JPQL query directive. It explicitly forces Hibernate to use an SQL `LEFT JOIN` or `INNER JOIN` in the generated query, resolving the N+1 problem for custom queries.

### Q2: Why is `@BatchSize` preferred over `@Fetch(FetchMode.SUBSELECT)` in high-scale paginated systems?
* **Answer:** Subselect executes a query containing the parent query as a subquery. If the parent query is complex (with multiple filters and joins), running it twice inside the subquery causes high database CPU utilization. `@BatchSize` fetches children in fixed-size packets (e.g., using `IN (?, ?, ...)` blocks), which is more performant and makes execution plans easily cacheable by the database.
