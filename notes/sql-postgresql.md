# SQL & PostgreSQL — Senior Interview Notes

## Joins
- `INNER JOIN`: matching rows only.
- `LEFT JOIN`: all left rows + matches; unmatched right side becomes NULL.
- `RIGHT JOIN`: symmetric to LEFT, used less often.
- `FULL OUTER JOIN`: rows from both sides, matched where possible.

## Aggregation
`WHERE` filters rows before grouping. `HAVING` filters groups after aggregation.

```sql
SELECT customer_id, COUNT(*)
FROM orders
WHERE status = 'PAID'
GROUP BY customer_id
HAVING COUNT(*) > 5;
```

## NULL
- `NULL = NULL` is not true; use `IS NULL`.
- SQL uses three-valued logic: TRUE / FALSE / UNKNOWN.
- `COUNT(column)` ignores NULL; `COUNT(*)` counts rows.

## EXISTS
`EXISTS` is useful when you only care whether a related row exists.

```sql
SELECT c.*
FROM customer c
WHERE EXISTS (
  SELECT 1 FROM orders o WHERE o.customer_id = c.id
);
```

## Indexes
Indexes improve reads by avoiding full scans but add write/storage cost.

Think about:
- equality/range predicates,
- join columns,
- sort order,
- selectivity,
- composite index column order,
- covering/index-only scans where possible.

Do not create an index for every column automatically.

## EXPLAIN / EXPLAIN ANALYZE
- `EXPLAIN`: estimated plan.
- `EXPLAIN ANALYZE`: executes query and shows actual timing/row counts.

Look for:
- sequential scans on large tables,
- bad row-count estimates,
- expensive sorts/hashes,
- nested-loop amplification,
- disk spills,
- repeated scans.

## ACID
- Atomicity: all-or-nothing transaction.
- Consistency: transaction preserves defined invariants.
- Isolation: concurrent transaction effects are controlled.
- Durability: committed changes survive failure according to DB guarantees.

## PostgreSQL MVCC
PostgreSQL uses MVCC so readers usually do not block writers and writers usually do not block readers. Rows have versions visible according to transaction snapshots.

Dead tuples remain until vacuum cleanup/reuse.

## Isolation
Default is `READ COMMITTED`.

- each statement sees a snapshot as of statement start,
- repeated queries in one transaction can see different committed values,
- row-level locks still matter for conflicting writes.

`REPEATABLE READ` gives one transaction snapshot and is stronger than SQL-standard minimum in PostgreSQL.

`SERIALIZABLE` can abort transactions to prevent serialization anomalies; application may need retry.

## Locks
`SELECT ... FOR UPDATE` locks selected rows against conflicting modifications.

Monitor blocking using `pg_stat_activity` and PostgreSQL lock views.

Avoid long transactions because they:
- retain locks,
- hold connections,
- delay vacuum cleanup,
- increase contention.

## VACUUM
Because MVCC leaves obsolete tuple versions, VACUUM marks space reusable and helps prevent transaction-ID problems. Autovacuum is critical production infrastructure.

## Pagination
Large `OFFSET` requires PostgreSQL to scan/skip many rows.

Keyset pagination:
```sql
SELECT *
FROM orders
WHERE id > :lastId
ORDER BY id
LIMIT 100;
```

Better scalability when ordering key is stable and indexed.

## Atomic inventory update
```sql
UPDATE inventory
SET quantity = quantity - 1
WHERE product_id = :id
  AND quantity > 0;
```

Affected rows = 1 means success; 0 means no stock. This avoids unsafe read-then-write races.

## Common traps
- An index is not always used just because it exists.
- More indexes can hurt write-heavy workloads.
- `EXPLAIN ANALYZE` executes the statement; be careful with mutating SQL.
- Long transactions harm concurrency and vacuum.
- `OFFSET` pagination becomes expensive at deep pages.

## Interview-ready summary
> I use EXPLAIN ANALYZE to compare estimated and actual execution behavior, design indexes around real predicates and ordering, understand PostgreSQL MVCC and locking, and prefer short transactions plus keyset pagination for large datasets.

## Review questions
1. WHERE vs HAVING?
2. Why does `NULL = NULL` not return true?
3. When is EXISTS preferable to a join?
4. What would you look for in EXPLAIN ANALYZE?
5. Why are long PostgreSQL transactions dangerous?
6. OFFSET vs keyset pagination?
