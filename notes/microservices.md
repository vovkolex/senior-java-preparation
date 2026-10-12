# Microservices — Senior Interview Notes

## Service boundaries
Good boundaries follow business capabilities and ownership, not arbitrary technical layers. Prefer independent data ownership: each service owns its database/schema and exposes behavior through APIs/events.

Microservices increase operational complexity. A modular monolith is often the better starting point when scale/team boundaries do not justify distribution.

## Distributed consistency
A local ACID transaction cannot atomically cover multiple services, Kafka and an external payment provider.

Use:
- Saga,
- Transactional Outbox,
- idempotency,
- retries/reconciliation,
- compensation where business-reversible.

## Saga
### Orchestration
A coordinator explicitly tracks steps and outcomes.

Pros: visible workflow/state. Cons: orchestrator complexity.

### Choreography
Services react to events without central coordinator.

Pros: loose coupling. Cons: harder to understand/debug as workflow grows.

Persist **durable Saga state**. Do not keep critical workflow state only in memory.

Example state:
```text
ORDER_CREATED
PAYMENT_PENDING
PAYMENT_SUCCEEDED
INVENTORY_RESERVED
COMPLETED
COMPENSATION_PENDING
FAILED
```

## Compensation
Compensation is a business operation, not a DB rollback.

Example:
- payment succeeded,
- inventory permanently unavailable,
- issue refund.

If compensation itself fails: persist `COMPENSATION_PENDING`, retry with backoff/jitter, use idempotency, alert, and support manual reconciliation.

## External payment calls
Use a stable provider idempotency key. If timeout/crash leaves outcome unknown, query provider by payment/idempotency key before charging again.

A successful external payment must not be assumed undone because local DB transaction rolled back.

## Resilience patterns
### Timeout
Every remote call needs a bounded timeout.

### Retry
Retry transient failures only; use exponential backoff + jitter. Avoid retry storms.

### Circuit Breaker
States: CLOSED → OPEN → HALF_OPEN. Stops repeated calls to unhealthy dependency.

### Bulkhead
Isolate resource pools/concurrency so one dependency cannot consume all threads/connections.

### Fallback
Degraded response when business semantics allow it. Never fabricate correctness-critical data.

## Sync vs async
Synchronous calls are simple when immediate response is required, but long call chains amplify latency and failures.

Async messaging improves decoupling/resilience but introduces eventual consistency and operational complexity.

Avoid distributed-monolith chains such as:
```text
A -> B -> C -> D -> E
```
where every user request depends synchronously on all services.

## API Gateway
Common responsibilities:
- routing,
- auth enforcement,
- rate limiting,
- request correlation,
- sometimes aggregation.

Do not move domain business logic into the gateway.

## Service discovery / Kubernetes
In Kubernetes, services commonly discover each other through Service DNS.

Probes:
- liveness: should container be restarted?
- readiness: should it receive traffic?
- startup: give slow-starting app time before liveness takes effect.

## Statelessness and scaling
Horizontal scaling is easiest when service instances are stateless with durable state in external stores.

Vertical scaling: bigger instance.
Horizontal scaling: more instances.

Microservices are not automatically more scalable than a well-designed monolith.

## CAP and consistency
During a network partition, a distributed system must trade off consistency vs availability for affected operations.

Read replicas can lag, causing stale reads. If read-your-writes matters, route appropriately or design a consistency strategy.

## Redis cache-aside
```text
GET -> L1/local cache
 miss -> Redis
 miss -> DB
 -> populate caches
```

Important issues:
- TTL,
- stale data,
- cache invalidation,
- stampede,
- negative caching.

Cache stampede fixes:
- single-flight/request coalescing,
- per-key lock,
- TTL jitter,
- stale-while-revalidate.

Negative caching solves repeated misses for nonexistent keys; it is not the same as stampede protection.

## Distributed locks
A JVM `synchronized` only protects one process. Distributed coordination may use Redis/DB/etc.

Production lock concerns:
- lease/TTL,
- owner token,
- renewal/heartbeat,
- failure while paused,
- fencing tokens to prevent stale lock holder from writing.

## Rate limiting
Gateway-level Token Bucket is common. When exceeded return HTTP `429 Too Many Requests` and optionally `Retry-After`.

## Deployment patterns
- Rolling: gradually replace instances.
- Blue-Green: switch traffic between two complete environments.
- Canary: send small traffic percentage to new version and observe.
- Feature flags: separate feature release from deployment.

## Service mesh
Can provide traffic policy, mTLS, retries, observability and routing between services. It does not remove the need for application-level idempotency and correct business semantics.

## Common traps
- Circuit breaker ≠ Saga compensation.
- Distributed lock ≠ durable Saga state.
- Retry without idempotency can duplicate side effects.
- Notification failure should usually not compensate a completed payment/order.
- Crash after successful Saga step often means **resume/recover**, not automatically compensate.

## Interview-ready summary
> I use microservices when service/team boundaries justify distribution. For cross-service workflows I use durable Saga state, Transactional Outbox and idempotent operations instead of pretending there is one global transaction. I combine timeouts, bounded retries, circuit breakers and bulkheads to stop cascading failures.

## Review questions
1. Saga orchestration vs choreography?
2. What do you do if payment succeeds but the service crashes before persisting the result?
3. How do you recover after Saga orchestrator restart?
4. What if two Pods try to execute the same Saga step?
5. Circuit breaker vs bulkhead?
6. How do you prevent cache stampede?
7. Why might a modular monolith be better than microservices?
