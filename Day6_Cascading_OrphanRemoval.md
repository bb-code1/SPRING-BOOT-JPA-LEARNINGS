# Day 6: Entity Cascading and Orphan Removal

## A. Concept
This module covers how state changes (persist, update, delete) propagate from a parent entity (`Order`) to its children (`OrderItem`) via Cascading and how `orphanRemoval` deletes disconnected children.

## B. Why it exists
* **Cascading:** Avoids writing boilerplate code. Instead of manually saving 10 items before saving the order, you save the order once and Hibernate cascades the persist operation.
* **Orphan Removal:** In a parent-child relationship (aggregate root), the child cannot exist without the parent. If you delete a line item from a shopping cart list, `orphanRemoval = true` ensures the orphaned line item is deleted from the database.

## C. Internal Working
* **`CascadeType.ALL`:** Propagates all state transitions: `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`.
* **`CascadeType.REMOVE` vs `orphanRemoval = true`:**
  * **`CascadeType.REMOVE`:** If you call `entityManager.remove(order)`, Hibernate deletes the associated `OrderItem` records. But if you call `order.getItems().remove(0)`, the parent remains, so **no database DELETE is triggered**. The record remains an orphan in the database.
  * **`orphanRemoval = true`:** Triggers a database `DELETE` in both cases. If you delete the order, or if you simply remove a single item from the `items` collection, Hibernate executes `DELETE FROM order_items WHERE id = ?`.

## D. Example
See [OrderService.java](src/main/java/com/zbs/learning/service/OrderService.java).

## E. SQL Generated
* When removing an item from the collection (`order.getItems().remove(...)`):
  ```sql
  DELETE FROM order_items WHERE id = ?;
  ```
  This is executed during the session flush phase before transaction commit.
* When deleting the parent order:
  ```sql
  DELETE FROM order_items WHERE order_id = ?;
  DELETE FROM orders WHERE id = ?;
  ```

## F. Common Mistakes
1. **Setting Cascades on the wrong side:** Putting `CascadeType.REMOVE` on the child `@ManyToOne` mapping. If you delete a single child order item, it will delete the parent order, which deletes all other items!
2. **Clearing collections incorrectly:** Calling `order.setItems(null)` on a collection mapped with `orphanRemoval = true`. Hibernate tracks collections using its own wrappers (`PersistentBag`). Reassigning the collection reference to `null` throws a `NullPointerException` or breaks Hibernate's dirty-checking tracking. Always use `order.getItems().clear()` to empty a collection.

## G. Production Considerations
In enterprise schemas, direct cascading deletes can lock child tables and cause locks contention. For massive child tables (e.g. deleting a department with 10,000 employees), it is safer to run a bulk delete query first before deleting the parent.

## H. Performance Considerations
* **Delete Propagation:** Hibernate executes deletes row-by-row for collection cascades. If you delete an order with 100 items, Hibernate executes 100 separate `DELETE` statements. If performance is critical, use bulk SQL delete queries instead.

## I. Senior-Level Discussion
Seniors understand that `orphanRemoval` acts at the JPA level, while database `ON DELETE CASCADE` acts at the schema level. If you define `ON DELETE CASCADE` in SQL, the database deletes child rows instantly without informing Hibernate. This can lead to stale L1 caches in Hibernate. Always coordinate JPA cascades and database constraints carefully.

## J. Interview Questions
1. **What is the difference between `CascadeType.REMOVE` and `orphanRemoval = true`?** (Remove only deletes children when the parent is deleted; orphan removal also deletes children when they are disconnected from the parent's collection reference).
2. **Why is calling `parent.setChildren(null)` a bad practice in JPA?** (It dereferences Hibernate's `PersistentBag` wrapper, breaking collection dirty-checking and throwing errors if `orphanRemoval` is active).
