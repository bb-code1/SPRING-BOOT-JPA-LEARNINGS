# Day 11: JPQL JOIN FETCH vs. JPA Entity Graph

## A. JPQL JOIN FETCH
**`JOIN FETCH`** is an HQL/JPQL statement directive that forces Hibernate to load associated entities in the same SQL SELECT statement using an SQL `INNER JOIN` or `LEFT JOIN`.
* **Execution:** Instead of loading parents and lazily loading children later, it fetches everything at once.
* **SQL Output:**
  ```sql
  SELECT o.*, i.* FROM orders o LEFT OUTER JOIN order_items i ON o.id = i.order_id;
  ```
* **Limitation (Cartesian Product):** Loading multiple collections with `JOIN FETCH` (e.g. `JOIN FETCH o.items JOIN FETCH o.payments`) causes a Cartesian Product, multiplying row counts in database memory and throwing `MultipleBagFetchException`.

## B. JPA Entity Graphs (`@EntityGraph`)
An **Entity Graph** is a declarative way to specify a template of attributes to be loaded eagerly.
* **`FETCH` Graph (Default):** All attributes specified in the graph are treated as `FetchType.EAGER`. All unspecified attributes are treated as `FetchType.LAZY`.
* **`LOAD` Graph:** All attributes specified in the graph are treated as `FetchType.EAGER`. All unspecified attributes retain their default fetch configurations (defined in their entity annotations).

## C. Comparison Table
| Feature | JPQL `JOIN FETCH` | JPA `@EntityGraph` |
| :--- | :--- | :--- |
| **Declaration** | Written inline inside JPQL query | Annotated on repository method |
| **SQL Join Type** | Explicit (`JOIN` or `LEFT JOIN`) | Left Outer Join by default |
| **Overriding Mapping**| Overrides Lazy config to Eager | Overrides Lazy config to Eager |
| **Flexibility** | Highly flexible for specific queries | Declarative and highly readable |

## D. Senior-Level Interview Q&A
### Q1: What is the difference between FETCH GRAPH and LOAD GRAPH?
* **Answer:**
  * **Fetch Graph (`jakarta.persistence.fetchgraph`):** The attributes specified are loaded eagerly. Everything else is forced to lazy, overriding any eager default annotations.
  * **Load Graph (`jakarta.persistence.loadgraph`):** The attributes specified are loaded eagerly. Unspecified fields retain their declared mapping types (so if a field is annotated `@ManyToOne(fetch = FetchType.EAGER)`, it remains eager).

### Q2: How do you fetch two independent collections on the same entity without throwing `MultipleBagFetchException`?
* **Answer:**
  1. **Change List to Set:** Map collections as `Set` instead of `List`. However, this still performs a Cartesian product in memory, which degrades performance for large datasets.
  2. **Multi-Query Loading:** Execute one query to fetch the parent entities with the first collection, and then execute a second query to fetch the parent entities with the second collection. The Persistence Context (L1 Cache) will automatically link the entities in memory.
