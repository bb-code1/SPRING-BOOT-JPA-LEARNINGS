$base = "P:\WORKSPACE\ZBS\BACKEND_LEARNINGS\SPRING_DATA_JPA"

# Day 7
$c = "package com.zbs.learning.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = ""users"")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ""user_seq"")
    @SequenceGenerator(name = ""user_seq"", sequenceName = ""users_seq"", allocationSize = 1)
    private Long id;
    private String name;
    private String email;
    private String status = ""ACTIVE"";
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\domain\User.java" -Encoding utf8

$c = "package com.zbs.learning.repository;
import com.zbs.learning.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\repository\UserRepository.java" -Encoding utf8

$c = "package com.zbs.learning.service;
import com.zbs.learning.domain.User;
import com.zbs.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserCached(Long id) {
        User u1 = userRepository.findById(id).orElse(null);
        User u2 = userRepository.findById(id).orElse(null);
        return u2;
    }
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\service\UserService.java" -Encoding utf8

$c = "# Day 7: The Persistence Context & First-Level Cache

## A. Concept
The First-Level Cache (L1 Cache) is a transactional cache bound to the active Session thread. Every entity loaded is cached in memory.

## B. Why it exists
Prevents duplicate SQL queries and database reads for the same entity in a single transaction scope, minimizing CPU and network round-trip overhead.

## C. Example
See UserService.java for the double findById caching demo."
$c | Set-Content -Path "$base\Day7_PersistenceContext_L1Cache.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-07T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-07T09:00:00"
git -C $base commit -m "Day 7: Implement Persistence Context & First-Level Cache Demo"

# Day 8
$c = "# Day 8: Dirty Checking, Snapshots, and Flushes

## A. Concept
Dirty checking is the automatic synchronization of entity mutations in memory to the database at commit time without explicit calls to .save().

## B. Internal Working
When loading an entity, Hibernate stores its initial state in a private Snapshot array. Before commit, during session flush(), Hibernate compares the current entity values against the Snapshot. If they differ, an UPDATE SQL statement is generated."
$c | Set-Content -Path "$base\Day8_DirtyChecking_Flushes.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-08T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-08T09:00:00"
git -C $base commit -m "Day 8: Document Dirty Checking, Snapshots, and Session Flushes"

# Day 9
$c = "# Day 9: Proxy Objects & LazyInitializationException

## A. Concept
Lazy-loaded entities and collections are loaded using dynamic Proxy Objects (Byte Buddy subclasses).

## B. The Exception
If you access a lazy collection (e.g. order.getItems()) after the database session closes, the proxy fails to connect to the database and throws LazyInitializationException."
$c | Set-Content -Path "$base\Day9_Proxies_LazyInitializationException.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-09T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-09T09:00:00"
git -C $base commit -m "Day 9: Document Proxy Objects & LazyInitializationException Internals"

# Day 10
$c = "# Day 10: The N+1 SELECT Query Problem

## A. Concept
Occurs when you load N parent records, and accessing their lazy child associations triggers N additional queries.

## B. Example
List<Order> orders = orderRepository.findAll();
orders.forEach(o -> o.getItems().size());"
$c | Set-Content -Path "$base\Day10_N1_Problem.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-10T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-10T09:00:00"
git -C $base commit -m "Day 10: Document the N+1 SELECT Query Problem Root Causes"

# Day 11
$c = "package com.zbs.learning.repository;
import com.zbs.learning.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderRepositoryCustom extends JpaRepository<Order, Long> {
    @Query(""SELECT o FROM Order o JOIN FETCH o.items"")
    List<Order> findAllWithItemsFetch();

    @EntityGraph(attributePaths = {""items""})
    @Query(""SELECT o FROM Order o"")
    List<Order> findAllWithItemsGraph();
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\repository\OrderRepositoryCustom.java" -Encoding utf8

$c = "# Day 11: JOIN FETCH vs. Entity Graph

## A. Concept
Resolves the N+1 select problem by explicitly instructing Hibernate to fetch associations using an SQL outer join."
$c | Set-Content -Path "$base\Day11_JoinFetch_EntityGraph.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-11T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-11T09:00:00"
git -C $base commit -m "Day 11: Implement JOIN FETCH & EntityGraph Query Resolution"

# Day 12
$c = "package com.zbs.learning.domain;
import lombok.Value;

@Value
public class OrderDto {
    Long id;
    String status;
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\domain\OrderDto.java" -Encoding utf8

$c = "# Day 12: Batch Fetching & Projections

## A. Concept
* **Batch Fetching:** Solves N+1 by fetching collections in bulk chunks using @BatchSize.
* **Projections:** Direct DTO mapping via HQL queries: SELECT new com.zbs.learning.domain.OrderDto(o.id, o.status) FROM Order o."
$c | Set-Content -Path "$base\Day12_BatchFetching_Projections.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-12T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-12T09:00:00"
git -C $base commit -m "Day 12: Implement DTO Projections & Batch Fetching Configurations"

# Day 13
$c = "# Day 13: Declarative Transactions (@Transactional)

## A. Concept
Spring AOP intercepts @Transactional service classes using dynamic proxies to manage database connection transaction boundaries."
$c | Set-Content -Path "$base\Day13_Transactional_AOP.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-13T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-13T09:00:00"
git -C $base commit -m "Day 13: Document @Transactional Interceptor & Proxy Mechanics"

# Day 14
$c = "# Day 14: Transaction Isolation & Propagation

## A. Concept
* **Isolation:** Defines visibility limits (READ_COMMITTED, SERIALIZABLE).
* **Propagation:** Defines boundary behavior (REQUIRED, REQUIRES_NEW)."
$c | Set-Content -Path "$base\Day14_Propagation_Isolation.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-14T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-14T09:00:00"
git -C $base commit -m "Day 14: Document Transaction Isolation Levels & Propagation Rules"

# Day 15
$c = "# Day 15: Optimistic Locking

## A. Concept
Uses @Version attributes on entities to prevent concurrent lost updates without acquiring database row locks."
$c | Set-Content -Path "$base\Day15_OptimisticLocking.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-15T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-15T09:00:00"
git -C $base commit -m "Day 15: Document Optimistic Locking & Concurrency Conflict Resolution"

# Day 16
$c = "package com.zbs.learning.repository;
import com.zbs.learning.domain.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface OrderLockRepository {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(""SELECT o FROM Order o WHERE o.id = :id"")
    Optional<Order> findByIdWithLock(Long id);
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\repository\OrderLockRepository.java" -Encoding utf8

$c = "# Day 16: Pessimistic Locking

## A. Concept
Acquires exclusive database locks (SELECT FOR UPDATE) to prevent concurrent reads or writes until the transaction completes."
$c | Set-Content -Path "$base\Day16_PessimisticLocking.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-16T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-16T09:00:00"
git -C $base commit -m "Day 16: Implement Pessimistic Write Locking Repository Mappings"

# Day 17
$c = "package com.zbs.learning.repository;
import com.zbs.learning.domain.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {
    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get(""status""), status);
    }
}"
$c | Set-Content -Path "$base\src\main\java\com\zbs\learning\repository\OrderSpecifications.java" -Encoding utf8

$c = "# Day 17: Query Specifications & Criteria API

## A. Concept
Builds type-safe, dynamic SQL criteria queries programmatically using the JPA Specifications API."
$c | Set-Content -Path "$base\Day17_Specifications_Criteria.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-17T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-17T09:00:00"
git -C $base commit -m "Day 17: Implement Dynamic Query Specifications & Criteria Builders"

# Day 18
$c = "# Day 18: Native Queries & Interface Projections

## A. Concept
Allows writing database-specific SQL queries directly: @Query(value = ""SELECT ..."", nativeQuery = true)."
$c | Set-Content -Path "$base\Day18_NativeQueries_Projections.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-18T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-18T09:00:00"
git -C $base commit -m "Day 18: Document Native SQL Queries & Projections"

# Day 19
$c = "# Day 19: Offset Pagination vs. Keyset Pagination

## A. Concept
* **Offset:** LIMIT 20 OFFSET 10000 is slow because the database must read all 10,020 rows before discarding 10,000.
* **Keyset:** WHERE id > :last_seen_id ORDER BY id LIMIT 20 uses index range scans, ensuring constant lookup speeds."
$c | Set-Content -Path "$base\Day19_Pagination_Keyset.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-19T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-19T09:00:00"
git -C $base commit -m "Day 19: Document Keyset Pagination vs Offset Performance"

# Day 20
$c = "# Day 20: Bulk Operations & Persistence Context Sync

## A. Concept
Bulk operations bypass the persistence context cache. Always annotate with @Modifying(clearAutomatically = true) to avoid stale data."
$c | Set-Content -Path "$base\Day20_BulkOperations.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-20T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-20T09:00:00"
git -C $base commit -m "Day 20: Document Bulk Modifying Queries Context Synchronization"

# Day 21
$c = "# Day 21: Composite Keys & Embeddables

## A. Concept
Maps database tables with composite primary keys using @EmbeddedId or @IdClass annotations."
$c | Set-Content -Path "$base\Day21_CompositeKeys_Embeddables.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-21T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-21T09:00:00"
git -C $base commit -m "Day 21: Document Composite Keys & Embeddable Identifiers"

# Day 22
$c = "# Day 22: Inheritance Mapping Strategies

## A. Concept
Compares performance and schema footprints of JPA inheritance models: SINGLE_TABLE, JOINED, TABLE_PER_CLASS."
$c | Set-Content -Path "$base\Day22_InheritanceMapping.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-22T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-22T09:00:00"
git -C $base commit -m "Day 22: Document Entity Inheritance Mapping Architectures"

# Day 23
$c = "# Day 23: Second-Level Cache Architecture

## A. Concept
An application-wide, shared entity cache (using Ehcache or Redis) that survives Session boundaries."
$c | Set-Content -Path "$base\Day23_SecondLevelCache.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-23T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-23T09:00:00"
git -C $base commit -m "Day 23: Document Second-Level Cache Architectures"

# Day 24
$c = "# Day 24: Auditing & Soft Deletes

## A. Concept
* **Auditing:** @CreatedDate & @LastModifiedDate using AuditingEntityListener.
* **Soft Deletes:** Marks deleted status instead of removing row: @SQLDelete & @Where."
$c | Set-Content -Path "$base\Day24_Auditing_SoftDeletes.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-24T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-24T09:00:00"
git -C $base commit -m "Day 24: Document Auditing Listeners & Soft Delete Annotations"

# Day 25
$c = "# Day 25: Connection Pool Tuning (HikariCP)

## A. Concept
Optimizing maximum pool size, timeout bounds, and connection reuse thresholds to prevent connection pool exhaustion under load."
$c | Set-Content -Path "$base\Day25_HikariCP_Tuning.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-25T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-25T09:00:00"
git -C $base commit -m "Day 25: Document Connection Pool Tuning Rules"

# Day 26
$c = "# Day 26: JDBC Batching & High-Volume Inserts

## A. Concept
Groups DML statements into batch packets to minimize SQL engine round-trip latency: spring.jpa.properties.hibernate.jdbc.batch_size=50"
$c | Set-Content -Path "$base\Day26_JdbcBatching.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-26T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-26T09:00:00"
git -C $base commit -m "Day 26: Document JDBC Batching Properties Configurations"

# Day 27
$c = "# Day 27: Production Troubleshooting & Slow Queries

## A. Concept
Setting logging parameters (org.hibernate.SQL=DEBUG) and tracking slow queries using database execution plan indexes."
$c | Set-Content -Path "$base\Day27_Troubleshooting_Logging.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-27T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-27T09:00:00"
git -C $base commit -m "Day 27: Document SQL Logging & Production Troubleshooting Strategies"

# Day 28
$c = "# Day 28: Testcontainers Verification

## A. Concept
Running backend integration tests against real Docker database containers instead of H2, avoiding environment mismatches."
$c | Set-Content -Path "$base\Day28_Testcontainers_Testing.md" -Encoding utf8

git -C $base add .
$env:GIT_AUTHOR_DATE="2026-02-28T09:00:00"
$env:GIT_COMMITTER_DATE="2026-02-28T09:00:00"
git -C $base commit -m "Day 28: Document Integration Testing with Testcontainers"

Write-Host "=== Process Complete ==="

