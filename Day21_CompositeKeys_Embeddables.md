# Day 21: Composite Keys & Embeddables

## A. Composite Keys in JPA
JPA supports two primary patterns to map tables with composite primary keys (multiple key columns):

1. **`@EmbeddedId` (Recommended):**
   * Primary key fields are grouped into a dedicated composite class annotated with `@Embeddable`.
   * The entity maps it using a single field annotated with `@EmbeddedId`.
2. **`@IdClass`:**
   * The composite fields are kept flat on the entity itself, each annotated with `@Id`.
   * The entity class points to the key class using the `@IdClass` annotation.

## B. Composite Key Class Rules
Whether using `@EmbeddedId` or `@IdClass`, the key class must satisfy these strict constraints:
* Must implement **`java.io.Serializable`**.
* Must implement **`equals()`** and **`hashCode()`** correctly (to support L1/L2 cache hash Lookups).
* Must have a public **no-argument constructor**.

## C. Implementation Comparison
* **EmbeddedId Model:**
  ```java
  @Embeddable
  public class OrderProductId implements Serializable {
      private Long orderId;
      private Long productId;
      // equals & hashCode
  }
  ```
  ```java
  @Entity
  public class OrderProduct {
      @EmbeddedId
      private OrderProductId id;
  }
  ```

## D. Senior-Level Interview Q&A
### Q1: Why must composite key classes override `equals()` and `hashCode()`?
* **Answer:** Hibernate uses the primary key value as the map key when caching entities in the L1 and L2 cache registries. If `equals()` and `hashCode()` are not implemented, Java defaults to memory address comparisons, causing cache lookup misses and duplicate entity creations.

### Q2: How do you map a `@ManyToOne` relationship that is part of a composite key?
* **Answer:** Use `@MapsId` on the relationship field. This maps the entity foreign key to the matching attribute inside the `@EmbeddedId` primary key:
  ```java
  @ManyToOne
  @MapsId("orderId") // Maps to OrderProductId.orderId
  @JoinColumn(name = "order_id")
  private Order order;
  ```
