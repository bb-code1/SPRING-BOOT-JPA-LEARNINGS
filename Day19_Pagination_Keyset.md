# Day 19: Offset Pagination vs. Keyset Pagination

## A. Concept
* **Offset:** LIMIT 20 OFFSET 10000 is slow because the database must read all 10,020 rows before discarding 10,000.
* **Keyset:** WHERE id > :last_seen_id ORDER BY id LIMIT 20 uses index range scans, ensuring constant lookup speeds.
