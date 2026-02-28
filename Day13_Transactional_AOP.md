# Day 13: Declarative Transactions (`@Transactional`) & AOP Proxies

## A. Transactional Proxy Architecture
Spring manages transactions declaratively using **Aspect-Oriented Programming (AOP)**.
* **Mechanism:** When a bean has methods annotated with `@Transactional`, Spring does not register the actual class instance directly in the application context. Instead, it creates a **Proxy Object** wrapping the target bean (using JDK Dynamic Proxy or CGLIB).
* **Interceptor Chain:**
  1. Client calls a method on the bean.
  2. The call hits the **`TransactionInterceptor`** on the proxy.
  3. The interceptor starts a new transaction (using `TransactionManager`).
  4. It delegates the call to the actual bean method inside a `try-catch` block.
  5. If the method returns successfully, the transaction commits. If a `RuntimeException` is thrown, it rolls back.

```
[Client] ---> [Spring Proxy Bean] ---> [TransactionInterceptor] ---> [Real Bean Method]
                     |                                                       |
            (Open Connection)                                      (Execute HQL/SQL)
```

## B. The Self-Invocation Trap
A major gotcha in Spring transaction management is **Self-Invocation**.
* **The Problem:** If a public method in Class A calls another method in Class A that is marked `@Transactional`, the transaction **will not start**.
  ```java
  public void processOrder(Long id) {
      saveOrderLog(id); // Trapped! Bypasses the transaction proxy.
  }
  @Transactional
  public void saveOrderLog(Long id) { ... }
  ```
* **Reason:** Method invocations within the same class are local calls bypassing the Spring AOP Proxy wrapper. The code executes directly on `this`, meaning the `TransactionInterceptor` is never triggered.

## C. Strategic Resolutions
1. **Self-Injection:** Inject the proxy bean of the same class (using `@Autowired @Lazy` or retrieving it from `ApplicationContext`):
   ```java
   TransactionDemoService self = applicationContext.getBean(TransactionDemoService.class);
   self.saveOrderLog(id);
   ```
2. **Refactoring:** Extract the transactional logic into a separate service class.

## D. Senior-Level Interview Q&A
### Q1: What type of exceptions trigger a transaction rollback by default?
* **Answer:** By default, Spring transactions roll back only on **unchecked exceptions** (inheriting from `RuntimeException` or `Error`). **Checked exceptions** (inheriting from `Exception`) do not trigger a rollback. You must explicitly configure `@Transactional(rollbackFor = Exception.class)` to force rollbacks on checked exceptions.

### Q2: What is the difference between CGLIB and JDK Dynamic Proxies?
* **Answer:**
  * **JDK Dynamic Proxy:** Used when the target class implements at least one interface. It creates a proxy implementing those interfaces.
  * **CGLIB Proxy:** Used when the target class does not implement interfaces. It dynamically generates a subclass of the target class at runtime, which is why transactional classes and methods cannot be marked `final`.
