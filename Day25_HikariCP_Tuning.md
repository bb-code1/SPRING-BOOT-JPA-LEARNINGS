# Day 25: Connection Pool Tuning (HikariCP)

## A. Connection Pool Dynamics
**HikariCP** is the default high-performance connection pool wrapper in Spring Boot.
* **The Goal:** Keep database connections open and ready in memory to avoid the massive TCP/IP handshake overhead of establishing database links for every query.

## B. Critical Settings
* **`maximum-pool-size` (Default: 10):** The maximum connections Hikari will allocate. If all are busy, new threads block until a connection is released.
* **`minimum-idle`:** The minimum idle connections Hikari keeps open.
  * **Tip:** **Set this equal to `maximum-pool-size`** to keep the pool fixed, avoiding runtime resizing latency.
* **`connection-timeout` (Default: 30000ms):** Maximum time a thread waits for a connection before throwing an `SQLTransientConnectionException`.
* **`idle-timeout`:** Time a connection remains idle before being closed.
* **`leak-detection-threshold`:** If set (e.g. `2000` for 2 seconds), Hikari will log a warning warning of connection leaks if a thread holds a connection longer than the limit.

## C. The Pool Size Equation
A common mistake is setting the pool size to a very high number (e.g. 500), thinking it increases throughput.
* **Formula:**
  ```
  connections = ((core_count * 2) + effective_spindle_count)
  ```
* **Reasoning:** A database can only run as many concurrent queries as it has CPU cores/disks. Allocating more connections forces the database to perform intensive context switching, degrading throughput.

## D. Senior-Level Interview Q&A
### Q1: Why should `minimum-idle` equal `maximum-pool-size` in production?
* **Answer:** Resizing pools at runtime (opening new database connections under load spikes) introduces high latency handshakes precisely when the application needs to be fastest. Keeping the pool size fixed ensures connection availability.

### Q2: How do you identify database connection leaks in production logs?
* **Answer:** Enable `spring.datasource.hikari.leak-detection-threshold=2000`. If a transaction takes longer than 2 seconds (e.g. due to uncommitted transactions, blocking calls inside transactions, or slow network I/O), Hikari will print a detailed stack trace showing where the connection was acquired.
