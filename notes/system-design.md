# System Design — Senior Interview Notes

## Design approach
A strong interview flow:
1. clarify functional requirements,
2. estimate scale,
3. define API/data model,
4. choose high-level architecture,
5. identify bottlenecks/failure modes,
6. discuss trade-offs.

Avoid premature sharding or exotic infrastructure before workload requires it.

## URL Shortener case study
Assumptions practiced:
- ~1M new short URLs/day,
- ~100M redirects/day,
- ~5 years retention,
- low redirect latency (e.g. p95 target around tens of ms).

This is strongly read-heavy; caching matters more than aggressive sharding at the start.

## API sketch
Create:
```http
POST /urls
Idempotency-Key: <client-generated-key>
```

Redirect:
```http
GET /{code}
```

Typical redirect response: HTTP `302 Found` when destination may change. Use cache headers according to product semantics.

## ID generation
Options:
- DB sequence/range allocation + Base62,
- Snowflake-like distributed IDs,
- random IDs with collision handling.

Range allocation reduces DB dependency while preserving uniqueness. Never claim plain local counters across Pods are globally unique without coordination.

## Storage
Simple relational model:
```text
short_url(code PK, original_url, owner_id, created_at, expires_at, ...)
```

PostgreSQL is enough for substantial scale before sharding is needed.

## Cache hierarchy
```text
L1 Caffeine per Pod
  -> L2 Redis
      -> PostgreSQL
```

Why L1:
- extremely low latency,
- reduces Redis hot-key pressure.

Trade-off: local cache invalidation/freshness.

## Cache invalidation
For mutable destination:
1. update PostgreSQL,
2. COMMIT,
3. invalidate Redis/local caches,
4. use TTL as a bounded-staleness safety net.

For reliable invalidation, publish invalidation event via outbox. Race windows still need to be considered.

## Cache stampede
When hot key expires, many requests hit DB simultaneously.

Mitigations:
- request coalescing/single-flight,
- Caffeine `cache.get(key, loader)`,
- distributed per-key lock when necessary,
- TTL jitter,
- stale-while-revalidate.

Negative caching solves repeated requests for missing keys; it is not the same problem.

## Hot keys
A URL receiving tens of thousands of redirects/sec can overload one Redis shard.

Mitigations:
- L1 cache on each application Pod,
- replication/read scaling where supported,
- hot-key-aware architecture,
- rate limiting only if business policy requires it.

Do not rate-limit legitimate redirects merely because a key is popular unless required.

## Redis outage
Graceful fallback:
```text
L1 cache
 -> Redis circuit breaker OPEN
 -> protected PostgreSQL fallback
```

Use:
- stale cache if business semantics allow,
- DB bulkhead/concurrency limit,
- single-flight,
- circuit breaker,
- aggressive monitoring.

Otherwise Redis outage can become PostgreSQL outage.

## Replication vs partitioning vs sharding
- **Replication**: copies of data for availability/read scale.
- **Partitioning**: split a logical dataset into parts; can exist inside one DB.
- **Sharding**: distribute partitions across independent database/server nodes.

Sharding increases routing, rebalancing, cross-shard query and operational complexity.

## Consistent hashing
Simple modulo:
```text
shard = hash(key) % N
```

When N changes, most keys remap.

Consistent hashing maps nodes/keys to a ring so adding/removing a node moves only a fraction of keys. Virtual nodes improve balance.

## Idempotent create API
Client sends stable `Idempotency-Key`.

Persist atomically:
```text
(user_id, idempotency_key) UNIQUE
request_hash
result_short_code
```

On retry:
- same key + same request -> return original result,
- same key + different request -> reject conflict.

Avoid check-then-insert race; rely on unique constraint / atomic upsert.

## Crash after commit before response
Scenario:
```text
DB COMMIT succeeds
Pod crashes before HTTP 201 reaches client
```

Client retries with same idempotency key. Server returns existing committed result rather than creating duplicate.

## Analytics
Redirect path should remain fast. Emit click event asynchronously and aggregate later.

Example:
```text
GET /abc -> redirect
          -> Kafka click event
                -> analytics consumers
                -> aggregates by day/country/device
```

If event publishing itself must be loss-resistant, design durable event capture carefully; fire-and-forget after response can lose events.

## Availability vs freshness
For a normal URL redirect, stale cache may be acceptable briefly. For a security takedown/abuse case, freshness may dominate availability.

State trade-off explicitly rather than saying consistency is always more important.

## Production incident reasoning
Example:
```text
p95 = 850 ms
error rate = 8%
Redis CPU = 95%
PostgreSQL connections = 95%
```

Do not start by deleting data or tuning randomly.

First:
- stabilize/rollback recent bad change if evidence supports it,
- inspect traces and dependency latency,
- Redis hot keys/evictions/network,
- Hikari active/idle/pending/acquisition time,
- PostgreSQL locks/slow queries/connection saturation,
- request/traffic changes.

Likely failure chain could be Redis saturation -> DB fallback surge -> pool exhaustion -> cascading latency/errors.

## Common traps
- Sharding too early.
- Treating Redis as mandatory for correctness.
- Using `201` for redirect response; redirects are typically `301/302/307/308` depending semantics.
- Generating idempotency key server-side after request arrives; client needs stable key across retries.
- Assuming synchronous analytics belongs on critical redirect path.

## Interview-ready summary
> I start with requirements and scale, choose the simplest storage that meets them, and design failure modes before adding complexity. For read-heavy systems I use layered caching and protect the database during cache outages. I use idempotency for retryable writes, and I add sharding only after evidence shows a single database is no longer sufficient.

## Review questions
1. Why is PostgreSQL initially sufficient for this URL-shortener workload?
2. How would you handle a 50k rps hot key?
3. What happens during Redis outage?
4. Why does modulo hashing rebalance badly when shard count changes?
5. How does client idempotency solve commit-before-response crashes?
6. What would you check first in a Redis + DB saturation incident?
