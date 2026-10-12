# Production, Performance & Observability — Senior Interview Notes

## Golden signals
Start with:
- latency (p50/p95/p99),
- traffic/throughput,
- error rate,
- saturation.

For Java services also watch:
- JVM heap/GC,
- thread pools,
- HikariCP active/idle/pending,
- CPU,
- pod restarts,
- Kafka lag,
- Redis latency/CPU/evictions,
- DB locks/slow queries/connections.

## Logs, metrics, traces
### Logs
Good for discrete events and detailed context. Use structured logs and correlation IDs. Avoid logging secrets/PII.

### Metrics
Good for trends, alerting and aggregate health.

### Distributed traces
Trace a request across services. Typical identifiers:
- traceId: whole request path,
- spanId: one operation.

Use traces to find which dependency/span contributes to p95/p99 latency.

## Percentiles
Average latency can hide tail problems. p95 means 95% of requests are at or below that latency; p99 shows worse tail behavior.

## HikariCP
`maximumPoolSize` is per application instance.

Example:
```text
10 Pods * maximumPoolSize 20 = up to 200 app DB connections
PostgreSQL max_connections = 150
```

Increasing pool size can make DB saturation worse.

`connectionTimeout=30000` means wait up to 30,000 ms to borrow a connection. It does not close a connection after 30 seconds.

Potential leak:
```java
Connection c = dataSource.getConnection();
// forgot close()
```

Use try-with-resources. With Hikari, `close()` on proxy connection returns it to pool.

`leakDetectionThreshold` helps log suspicious long-held connections; it does not reclaim them.

## Long transactions
A transaction that performs DB work and then waits 10 seconds for external HTTP can:
- hold pool connection,
- retain locks,
- increase DB contention,
- make requests queue for connections,
- cause cascading timeouts.

Keep local DB transaction short. Use Saga/Outbox for distributed workflows.

## Thread and connection exhaustion
Symptoms:
- high pending Hikari requests,
- request latency rising sharply,
- servlet/reactor executor saturation,
- timeout errors,
- dependency may still look partially healthy.

Do not automatically add more threads/connections; identify the bottleneck.

## Incident workflow
1. Assess customer impact and blast radius.
2. Check recent deploy/config/traffic changes.
3. Stabilize: rollback/disable feature/limit traffic if evidence supports it.
4. Use metrics to identify saturated resource.
5. Use traces to locate latency.
6. Use logs for detailed errors.
7. Inspect DB/Redis/Kafka/thread dumps as needed.
8. Form and test a hypothesis.
9. Fix root cause, then add prevention/alerting.

Avoid destructive actions like deleting data without a verified hypothesis.

## Database incident checks
- Hikari active/idle/pending,
- acquisition time,
- `pg_stat_activity`,
- blocked/long-running transactions,
- lock waits,
- slow queries,
- query plans,
- CPU/I/O,
- connection count.

## Redis incident checks
- CPU,
- memory,
- hit rate,
- evictions,
- latency,
- hot keys,
- network,
- command distribution.

A cache outage/saturation can create a thundering herd against PostgreSQL. Protect fallback using bulkheads, rate/concurrency limits, L1 cache and single-flight.

## Kafka incident checks
- consumer lag,
- rebalance frequency,
- under-replicated partitions,
- producer error/retry rate,
- processing duration,
- poison messages / retry loops.

## Timeouts and retry budgets
A caller timeout should align with downstream timeout/retry behavior. Nested retries across services can multiply traffic dramatically.

Prefer one clear retry layer where possible, use exponential backoff + jitter, and bound total time.

## Interview-ready summary
> In production incidents I start from customer impact and the golden signals, correlate metrics with traces and logs, and identify the saturated resource before tuning. I avoid blindly increasing Hikari pools or retries because both can amplify cascading failures. I keep transactions short and use bounded timeouts, backoff and bulkheads around dependencies.

## Review questions
1. Why are p95/p99 more useful than average for tail latency?
2. What does Hikari `connectionTimeout` mean?
3. How can Redis failure cause PostgreSQL failure?
4. What would you inspect when DB connections are 95% utilized?
5. Why can retries create an outage?
6. What is the difference between logs, metrics and traces?
