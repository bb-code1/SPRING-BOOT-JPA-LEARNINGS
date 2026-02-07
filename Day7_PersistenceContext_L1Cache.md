# Day 7: The Persistence Context & First-Level Cache

## A. Concept
The First-Level Cache (L1 Cache) is a transactional cache bound to the active Session thread. Every entity loaded is cached in memory.

## B. Why it exists
Prevents duplicate SQL queries and database reads for the same entity in a single transaction scope, minimizing CPU and network round-trip overhead.

## C. Example
See UserService.java for the double findById caching demo.
