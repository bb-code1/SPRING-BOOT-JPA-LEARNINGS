# Day 8: Dirty Checking, Snapshots, and Flushes

## A. Concept
Dirty checking is the automatic synchronization of entity mutations in memory to the database at commit time without explicit calls to .save().

## B. Internal Working
When loading an entity, Hibernate stores its initial state in a private Snapshot array. Before commit, during session flush(), Hibernate compares the current entity values against the Snapshot. If they differ, an UPDATE SQL statement is generated.
