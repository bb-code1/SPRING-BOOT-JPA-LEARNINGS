# Day 17: Query Specifications & Criteria API

## A. JPA Criteria API
The **JPA Criteria API** is a type-safe, programmatic way to construct database queries in Java. It is highly structured but verbose.
* **Components:**
  * `CriteriaBuilder`: Factory class to create query definitions, expressions, and predicates.
  * `CriteriaQuery`: Defines the query structure (SELECT, FROM, WHERE, ORDER BY).
  * `Root`: Points to the source entity in the query (defines the FROM clause).

## B. Spring Data JPA Specifications
Spring Data JPA wraps the Criteria API using the **Specification** interface, implementing the *Specification Design Pattern*.
* **Interface:**
  ```java
  public interface Specification<T> {
      Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
  }
  ```
* **Composition:** You can dynamically combine specifications using logical operators:
  ```java
  Specification<Order> spec = Specification.where(hasStatus("PENDING"))
                                           .and(createdAfter(startDate));
  List<Order> results = orderRepository.findAll(spec);
  ```

## C. Production Usage
To use Specifications, your repository interface must extend `JpaSpecificationExecutor<T>`:
```java
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {}
```

## D. Senior-Level Interview Q&A
### Q1: Why do we use Specifications instead of writing dynamic JPQL query strings?
* **Answer:** Dynamic JPQL string concatenation is prone to syntax errors, SQL injection vulnerabilities, and lacks compiler safety check support. Specifications build queries programmatically using strongly-typed criteria metadata, guaranteeing syntactic correctness and type safety during compile time.

### Q2: How do you perform joins using the Criteria API inside a Specification?
* **Answer:** Use the `root.join()` method to fetch associated entities:
  ```java
  Specification<Order> spec = (root, query, cb) -> {
      Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);
      return cb.equal(items.get("productName"), "Java Book");
  };
  ```
