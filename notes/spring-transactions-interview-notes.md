# Spring Transactions — Senior Java Interview Notes

> Interview revision notes | Updated: 2026-10-12 | Spring Boot / Spring Framework / PostgreSQL

## 1. Mental model

`@Transactional` defines a transactional boundary around a Spring-managed method invocation. In the usual proxy-based mode, a caller must invoke the method **through the Spring proxy**. Spring's transaction interceptor starts or joins a transaction, invokes the method, then commits or rolls back according to its rules.

- A **logical transaction scope** is a Spring `@Transactional` boundary. Multiple `REQUIRED` scopes can participate in **one physical database transaction**.
- A transaction is **not** a distributed transaction across HTTP services, Kafka, and PostgreSQL.
- Keep DB transactions **short**. Avoid slow HTTP calls, blocking operations, and lengthy CPU work while holding a connection/locks.
- `@Transactional` is not a substitute for concurrency control, retries, idempotency, or Saga orchestration.

## 2. Proxy and self-invocation

```java
@Service
class OrderService {
    public void createOrder() {
        saveOrder(); // this.saveOrder(): bypasses proxy
    }

    @Transactional
    public void saveOrder() { /* ... */ }
}
```

In proxy mode, the annotation on `saveOrder()` is **not applied** when called internally via `this`. Fix by putting `@Transactional` on an externally invoked method, or moving `saveOrder()` into another Spring bean and calling it through its proxy. `private` methods and constructors are not appropriate proxy-based transactional entry points. Actual proxy behavior can depend on proxy type and method visibility; public service methods are the clearest default.

**Interview answer:** “Self-invocation bypasses the Spring AOP proxy, so the transaction interceptor is not triggered for the internally invoked method.”

## 3. Propagation cheat sheet

| Propagation | Existing transaction | No existing transaction | Typical use |
|---|---|---|---|
| `REQUIRED` (default) | Join | Create | Normal business operations |
| `REQUIRES_NEW` | Suspend outer; create independent physical transaction | Create | Independent audit/outbox-like work (use cautiously) |
| `SUPPORTS` | Join | Run without transaction | Optional transactional reads |
| `MANDATORY` | Join | Throw exception | Must be called inside a transaction |
| `NOT_SUPPORTED` | Suspend; run without transaction | Run without transaction | Nontransactional work |
| `NEVER` | Throw exception | Run without transaction | Forbid transaction |
| `NESTED` | Savepoint within physical transaction when supported | Behaves like `REQUIRED` | Partial rollback using savepoints |

`NESTED` depends on transaction manager and savepoint support (commonly JDBC/DataSourceTransactionManager); it is **not** an independent transaction like `REQUIRES_NEW`.

### `REQUIRED` + caught exception: surprising rollback

```java
@Transactional
public void createOrder() {
    orderRepository.save(new Order());
    try {
        auditService.saveAudit(); // another Spring bean
    } catch (RuntimeException e) {
        log.warn("Audit failed", e);
    }
}

@Transactional(propagation = Propagation.REQUIRED)
public void saveAudit() {
    auditRepository.save(new Audit());
    throw new RuntimeException("Audit failed");
}
```

Both scopes share the same physical transaction. The exception exits the inner proxied method, so Spring marks the shared transaction **rollback-only**. Catching it in the outer method does not clear that flag. The outer commit rolls back and typically throws `UnexpectedRollbackException`. **Neither Order nor Audit is committed.**

### `REQUIRES_NEW` + caught exception

Change only the inner annotation to `REQUIRES_NEW`: outer transaction is suspended, inner audit transaction rolls back, exception is caught, outer order transaction can **commit**. Beware of extra connections: a suspended outer transaction may retain its DB connection while the inner one needs another. Under concurrent load this can exhaust HikariCP.

## 4. Rollback rules: checked vs unchecked

By default, Spring rolls back for `RuntimeException` and `Error`, **not checked exceptions** such as `IOException`.

```java
@Transactional
public void processPayment() throws IOException {
    paymentRepository.save(new Payment());
    throw new IOException("Provider unavailable");
}
```

Under normal conditions, Spring attempts to **commit** the DB transaction even though `IOException` propagates. To roll back:

```java
@Transactional(rollbackFor = IOException.class)
public void processPayment() throws IOException { /* ... */ }
```

`rollbackFor = Exception.class` is broader and should be chosen intentionally. A caught exception that never reaches the transactional interceptor does not automatically trigger rollback (unless the transaction was separately marked rollback-only). Some Spring versions/configurations support different global rollback defaults; know your application's configuration.

## 5. Isolation levels and anomalies

| Isolation | Dirty reads | Non-repeatable reads | Phantom reads (SQL standard) |
|---|---|---|---|
| `READ_UNCOMMITTED` | Possible | Possible | Possible |
| `READ_COMMITTED` | Prevented | Possible | Possible |
| `REPEATABLE_READ` | Prevented | Prevented | Standard allows phantoms |
| `SERIALIZABLE` | Prevented | Prevented | Prevented |

**PostgreSQL specifics:** `READ_UNCOMMITTED` behaves as `READ_COMMITTED`; PostgreSQL's `REPEATABLE READ` uses snapshot isolation and prevents phantom reads as defined by the SQL standard, but serialization anomalies can still occur. `SERIALIZABLE` may abort transactions with serialization failures; retry the **whole transaction** when appropriate. Default PostgreSQL isolation is `READ COMMITTED`.

`@Transactional(isolation = Isolation.SERIALIZABLE)` affects a **newly started** transaction; if joining an existing transaction, the existing transaction's isolation usually governs.

## 6. Locking and lost updates

**Optimistic locking**: `@Version` on a JPA entity; concurrent conflicting updates cause an optimistic locking exception. Useful when contention is low; retry after reloading when safe.

```java
@Version
private Long version;
```

**Pessimistic locking**: `SELECT ... FOR UPDATE` / JPA `PESSIMISTIC_WRITE`; holds a row lock until transaction completion. Useful for contentious inventory or booking operations, but introduces waiting and deadlock risks.

**Atomic conditional update** can be simpler for stock:

```sql
UPDATE inventory
SET available = available - 1
WHERE product_id = :id AND available > 0;
```

Check affected row count. Do not implement read-then-write without concurrency protection.

## 7. JPA transaction details

- **Persistence Context** tracks managed entities; dirty checking schedules updates.
- `flush()` sends pending SQL to the DB but **does not commit**. A later rollback undoes those changes.
- `commit()` makes transaction changes durable/visible according to isolation rules.
- `save()` does not guarantee immediate SQL execution; behavior depends on entity state, ID strategy, flush mode, etc.
- Bulk JPQL updates bypass normal managed-entity synchronization; consider clearing/refreshing persistence context.
- `LazyInitializationException` can occur if a lazy association is accessed outside an active persistence context. Fetch needed data within a suitable service boundary; avoid relying on Open Session in View.

## 8. External HTTP calls inside `@Transactional`

**Anti-pattern:**

```java
@Transactional
public void createOrder(OrderRequest request) {
    orderRepository.save(new Order(request));
    paymentClient.charge(request.payment()); // may block 10 seconds
    orderRepository.updateStatus(request.orderId(), "PAID");
}
```

The database connection/locks may be held while waiting on HTTP, exhausting the connection pool under load. Also, rolling back the DB does **not undo a successful external payment**.

**Better:** commit `Order(PENDING)` and an `OrderCreated` outbox event in one local transaction; asynchronously process payment; persist Saga state; publish outcome; update order in another short transaction. Handle provider idempotency, unknown outcomes, retries, and compensation where needed.

## 9. Transactional Outbox

```text
BEGIN
  INSERT orders (..., status='PENDING');
  INSERT outbox_events (..., type='OrderCreated');
COMMIT

Outbox publisher → Kafka → idempotent consumers
```

DB transaction atomically persists business state and event intent. Kafka publication is **not** part of the same PostgreSQL transaction. A crash after publishing but before marking outbox as delivered can cause duplicates. Consumers need idempotency/deduplication. Avoid claiming global exactly-once processing across Kafka, database, and payment provider.

## 10. HikariCP and transactions

- `maximumPoolSize`: maximum DB connections **per application instance/Pod**.
- `connectionTimeout`: maximum time to **wait for a pool connection**, in **milliseconds**; not SQL execution timeout or connection lifetime.
- `maxLifetime`: maximum age of a pooled connection; does not forcibly interrupt an in-use query.
- `leakDetectionThreshold`: logs potential connection leaks; does not reclaim connections.
- Ten Pods × pool size 20 = **up to 200** application connections; this can exceed PostgreSQL `max_connections=150`.
- `REQUIRES_NEW` can need another connection while the outer transaction's connection remains held.
- Use `try-with-resources` for manually acquired JDBC connections. `Connection.close()` on a pooled connection normally **returns it to the pool**.

```java
try (Connection c = dataSource.getConnection();
     PreparedStatement ps = c.prepareStatement("SELECT * FROM orders");
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) { /* process */ }
}
```

Production diagnostics: Hikari active/idle/pending metrics, connection acquisition time, PostgreSQL `pg_stat_activity`, locks, slow queries, `EXPLAIN ANALYZE`, distributed traces, recent deployments, and traffic spikes. Do not blindly increase pool size.

## 11. Common interview traps

| Trap | Correct answer |
|---|---|
| `REQUIRED` always creates a new transaction | No; it joins an existing transaction |
| Catching an inner `REQUIRED` exception always permits commit | No; inner interceptor may mark shared transaction rollback-only |
| `REQUIRES_NEW` is free | No; separate transaction, potentially another connection |
| `@Transactional` on `this.method()` works | Not in default proxy mode |
| `IOException` triggers rollback by default | Usually no; specify `rollbackFor` |
| `flush()` equals `commit()` | No |
| Increasing Hikari pool always improves throughput | No; may overwhelm PostgreSQL |
| `connectionTimeout=30000` closes connection after 30 seconds | No; it bounds waiting for a connection |
| PostgreSQL transaction can undo a successful payment HTTP call | No; requires distributed workflow/reconciliation |
| Outbox guarantees zero duplicate Kafka messages | No; consumers must handle duplicates |

## 12. Rapid-fire self-test

1. Why does `@Transactional` fail with self-invocation?
2. `REQUIRED` vs `REQUIRES_NEW`: physical transactions and rollback behavior?
3. Why can an outer method throw `UnexpectedRollbackException` after catching an inner exception?
4. Does `@Transactional` roll back on `IOException` by default? How to change it?
5. What is the difference between `flush` and `commit`?
6. What anomalies are possible at PostgreSQL `READ COMMITTED`?
7. When would you use optimistic vs pessimistic locking?
8. Why avoid HTTP calls inside DB transactions?
9. How does Transactional Outbox prevent the DB-write/event-intent dual-write problem?
10. With 10 Pods and `maximumPoolSize=20`, what is the maximum application DB connection count?
11. What happens when all Hikari connections are busy for longer than `connectionTimeout`?
12. What are the trade-offs of `REQUIRES_NEW` under high concurrency?

## 13. 30-second interview summary

> Spring transactions are usually implemented through AOP proxies. The default propagation is REQUIRED, which joins an existing transaction. REQUIRES_NEW starts an independent transaction and may require an additional connection. By default, runtime exceptions trigger rollback, while checked exceptions generally do not unless configured. Self-invocation bypasses proxy advice. I keep transactions short, use optimistic or pessimistic locking according to contention, avoid external HTTP calls inside transactions, and use Saga plus Transactional Outbox for distributed workflows.

---

**Revision priority based on our practice:** `REQUIRED` vs `REQUIRES_NEW`; rollback-only and `UnexpectedRollbackException`; checked exception rollback; HikariCP pool sizing and connection timeouts. Revisit these first.
