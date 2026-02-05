# Day 5: The `@OneToMany` & `@ManyToMany` Pitfalls

## A. Concept
This module covers why raw `@ManyToMany` relationships are an architectural bottleneck in production databases, and how to use the **Intermediate Entity Pattern** (a junction entity) instead.

## B. Why it exists
Suppose you map `Order` and `Product` with a direct `@ManyToMany` annotation. Hibernate creates a hidden join table (`orders_products`) containing only `order_id` and `product_id`.
* **The Pitfall:** You cannot add any metadata to this relationship. For example, you cannot record the **quantity** of the product ordered, the **historical price** at purchase, or specific **discounts**.
* **The Solution:** Split the `@ManyToMany` into two `@ManyToOne` relationships referencing an intermediate junction entity (`OrderItem`).

## C. Internal Working
* **Junction Entity (`OrderItem`):** Contains references to both parent entities:
  * `@ManyToOne` to `Order`
  * `@ManyToOne` to `Product`
* **Additional Attributes:** The junction entity can store additional transactional columns like `quantity` and `price`.
* **Historical Auditing:** Although `Product` has a current `price` column, `OrderItem` also has a `price` column. This is intentional. If the product price changes in the catalog tomorrow, the historical price of old orders must remain locked in inside `OrderItem`.

## D. Example
See [Product.java](src/main/java/com/zbs/learning/domain/Product.java) and [OrderItem.java](src/main/java/com/zbs/learning/domain/OrderItem.java).

## E. SQL Generated
When saving an order with items and products:
```sql
-- Step 1: Insert parent order
INSERT INTO orders (order_date, status, id) VALUES (?, ?, ?);
-- Step 2: Insert junction records referencing the order and product
INSERT INTO order_items (price, order_id, product_id, quantity, id) VALUES (?, ?, ?, ?, ?);
```

## F. Common Mistakes
Using raw `@ManyToMany` for business-critical relations:
```java
// BAD DESIGN:
@ManyToMany
private List<Product> products;
// You cannot define quantity or transactional price!
```

## G. Production Considerations
* **Indexing:** Always create foreign key indexes on the junction table columns (`order_id` and `product_id`) to prevent Full Table Scans during joins.
* **Cascade rules:** Cascades should run from `Order` to `OrderItem`, but **never** cascade deletes from `OrderItem` to `Product` (deleting an order item should not delete the actual product catalog item!).

## H. Performance Considerations
* **N+1 Hazard:** Fetching orders and resolving products recursively causes N+1 queries. Always fetch using `JOIN FETCH` or batching when traversing.

## I. Senior-Level Discussion
Seniors avoid raw `@ManyToMany` because they serialize and delete entire join tables on updates. Hibernate's internal collection management for `@ManyToMany` deletes all rows in the join table and re-inserts them when a single item is modified, which degrades performance on large associations.

## J. Interview Questions
1. **Why is raw `@ManyToMany` avoided in e-commerce database design?** (Because you cannot add relationship attributes like quantity or purchase price to the join table).
2. **How does Hibernate handle updates to a raw `@ManyToMany` List collection?** (It deletes all entries in the join table for that parent ID and re-inserts them, causing high DML log writes).
