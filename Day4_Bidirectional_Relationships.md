# Day 4: Bidirectional Relationship Mechanics

## A. Concept
Bidirectional relationship mapping allows you to traverse associations in both directions (e.g. from `Order` to `OrderItem` and vice versa).

## B. Why it exists
In real systems, you need to query both ways:
1. Fetching an `Order` and displaying its items in the UI.
2. Fetching an `OrderItem` and querying the parent order details (e.g., status or order date).

## C. Internal Working
* **The Owning Side:** The table containing the Foreign Key (FK) column is **always** the owning side of the relationship in JPA. Here, `OrderItem` contains the `order_id` column, so it is marked with `@ManyToOne` and `@JoinColumn`.
* **The Non-Owning Side:** The parent class (`Order`) defines the relationship using `mappedBy = "order"`. This tells Hibernate that `OrderItem` manages the mapping.
* **Helper Sync Methods:** In JVM memory, Java does not automatically maintain reference synchronization. If you call `order.getItems().add(item)`, the item's internal `order` reference remains `null`. When saving, Hibernate reads the owning side (`OrderItem.order`). If it is null, it inserts a `NULL` foreign key (throwing a database constraint violation). You must use sync helper methods (`addItem`, `removeItem`) to bind both sides in memory.

## D. Example
See [Order.java](src/main/java/com/zbs/learning/domain/Order.java) and [OrderItem.java](src/main/java/com/zbs/learning/domain/OrderItem.java).

## E. SQL Generated
When saving an order with items, Hibernate inserts the parent first, retrieves the sequence, and then inserts the child rows matching the parent key:
```sql
INSERT INTO orders (order_date, status, id) VALUES (?, ?, ?);
INSERT INTO order_items (price, order_id, product_name, quantity, id) VALUES (?, ?, ?, ?, ?);
```

## F. Common Mistakes
Failing to link both sides in memory:
```java
// BUG: order_id will be inserted as NULL in the database
Order order = new Order();
OrderItem item = new OrderItem();
order.getItems().add(item); // Missing item.setOrder(order)
orderRepository.save(order);
```

## G. Production Considerations
* **Cascading:** Use `CascadeType.ALL` only when the child lifecycle is tightly coupled to the parent (like order items belonging to an order).
* **orphanRemoval = true:** Ensures that removing an item from the collection (`order.removeItem(item)`) generates a physical `DELETE` statement in the database, avoiding orphan rows.

## H. Performance Considerations
* **Eager Loading Trap:** Never set `fetch = FetchType.EAGER` on `@OneToMany`. It causes Hibernate to fetch child records automatically, generating massive unnecessary joins or queries.

## I. Senior-Level Discussion
Seniors know that bidirectional relationships add memory management complexity. In high-scale systems, sometimes it is better to use **unidirectional `@ManyToOne`** relationships only, and query the children via separate repository calls to avoid caching overhead.

## J. Interview Questions
1. **What is the purpose of `mappedBy`?** (Defines the non-owning side of the relationship).
2. **What is the difference between `cascade = CascadeType.REMOVE` and `orphanRemoval = true`?** (Remove deletes children when the parent is deleted. Orphan removal deletes child rows when they are simply removed from the parent's collection).
