# Day 28: Testcontainers Integration Verification

## A. Why avoid H2 for Integration Testing?
Many teams use **H2 Database** in memory for integration testing because it is fast.
* **Dialect Mismatch:** H2 behaves differently from production databases (PostgreSQL/Oracle) regarding concurrency locks, window functions, native SQL formats, JSON operations, and date manipulations.
* **False Security:** Tests pass on H2 but fail in production.

## B. Testcontainers
**Testcontainers** is a Java library that allows spinning up real databases in Docker containers during JUnit integration tests.
* **Execution:** A real PostgreSQL Docker container is spun up before test suite execution.
* **Configuration:** Spring Boot binds the datasource properties dynamically:
  ```java
  @SpringBootTest
  @Testcontainers
  public class IntegrationTest {
      @Container
      static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
  }
  ```

## C. The Singleton Container Pattern
Starting a new container for every test class is slow (adding 5-10s per class).
* **Fix:** Use the **Singleton Container Pattern** by starting the container once inside a base class and sharing its properties across all test classes, speeding up verification.

## D. Senior-Level Interview Q&A
### Q1: How does Testcontainers integrate with Spring Boot dynamically?
* **Answer:** Spring Boot 3 introduced `@ServiceConnection` or `@DynamicPropertySource` to bind the container's dynamic port and credentials to the database connection pool:
  ```java
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);
  }
  ```

### Q2: Why are schema migrations (Flyway/Liquibase) critical when using Testcontainers?
* **Answer:** Testcontainers starts a completely empty database container. Running schema migrations validates that your SQL schema scripts are correct, verifying index creations and constraint configurations against a real database during the build.
