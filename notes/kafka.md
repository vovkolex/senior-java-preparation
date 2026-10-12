# Kafka & Messaging — Senior Interview Notes

## Core model
- Topic is split into partitions.
- Ordering is guaranteed only within a partition.
- Consumer group distributes partitions among consumers; one partition is owned by at most one consumer in the same group at a time.
- Different consumer groups can independently read the same topic.

## Keys and ordering
Messages with the same key are typically routed to the same partition, preserving order for that key.

Use a business key such as `orderId` when per-entity ordering matters.

## Offsets
Consumer offset represents progress within a partition.

Important distinction:
- processing message,
- committing offset.

If offset is committed before business processing is durably complete, failures can lose work. If committed after, failures can cause duplicates.

## Delivery semantics
### At-most-once
Commit/ack before processing. No duplicates, but messages can be lost.

### At-least-once
Process first, commit after success. No intentional loss, but duplicates are possible.

### Exactly-once
Kafka transactions can provide exactly-once semantics for Kafka read-process-write flows under the right configuration. They do **not** magically make PostgreSQL or external payment providers part of one atomic transaction.

## Idempotent consumer
Typical approach:
- stable event/message ID,
- deduplication table with unique key,
- or naturally idempotent update/business operation.

Example:
```sql
INSERT INTO processed_events(event_id)
VALUES (:id)
ON CONFLICT DO NOTHING;
```

Perform deduplication and business change atomically in one local DB transaction where possible.

## Producer durability
`acks=all` waits for all in-sync replicas required by broker rules. Durability also depends on replication factor, ISR and `min.insync.replicas`.

Idempotent producer prevents certain duplicate writes caused by producer retries.

## Retention vs compaction
- Retention by time/size: delete old log segments.
- Log compaction: retain latest value per key over time (plus tombstone semantics).

Compacted topics suit changelog/state topics, not every event-history use case.

## Retry and DLT
Avoid endless tight retry loops.

Typical pattern:
```text
main topic
 -> transient failure
 -> retry with backoff
 -> retry topic(s)
 -> DLT after max attempts
```

Retry only failures likely to recover. Validation/business-invalid messages often belong directly in DLT/manual handling.

## Transactional Outbox
Write business data + outbox record in the same DB transaction.

Then publish via:
- polling publisher,
- CDC such as Debezium reading WAL.

This avoids the DB/Kafka dual-write gap, but duplicate delivery remains possible, so consumers must be idempotent.

## Debezium
Debezium can read PostgreSQL WAL and emit row changes. Useful for outbox CDC. It is not a substitute for thoughtful event contracts and idempotency.

## Schema evolution
Schema Registry (Avro/Protobuf/JSON Schema) can enforce compatibility rules.

Prefer backward-compatible evolution:
- add optional/defaulted fields,
- avoid removing/renaming required fields without version strategy.

## Rebalancing
Consumer-group membership/partition changes can trigger rebalancing. During assignment changes processing may pause. Keep processing time, poll interval and consumer configuration aligned.

## Common traps
- Kafka does not provide global ordering across partitions.
- `acks=all` is not the same as 'cannot lose data under any failure'.
- Exactly-once Kafka does not make HTTP payments exactly once.
- Outbox solves dual-write consistency, not duplicate consumption.
- DLT is not a place to silently forget failures.

## Interview-ready summary
> I design Kafka flows around partition-level ordering and at-least-once delivery, make consumers idempotent, use Transactional Outbox to bridge database state and event publication, and treat Kafka exactly-once as scoped to Kafka rather than a distributed transaction across external systems.

## Review questions
1. What ordering does Kafka guarantee?
2. What happens when consumers exceed partition count?
3. At-least-once vs at-most-once?
4. Why must outbox consumers still be idempotent?
5. Retention vs compaction?
6. What can trigger a consumer rebalance?
