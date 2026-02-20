# Day 20: Bulk Operations & Persistence Context Sync

## A. Concept
Bulk operations bypass the persistence context cache. Always annotate with @Modifying(clearAutomatically = true) to avoid stale data.
