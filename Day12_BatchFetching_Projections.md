# Day 12: Batch Fetching & Projections

## A. Concept
* **Batch Fetching:** Solves N+1 by fetching collections in bulk chunks using @BatchSize.
* **Projections:** Direct DTO mapping via HQL queries: SELECT new com.zbs.learning.domain.OrderDto(o.id, o.status) FROM Order o.
