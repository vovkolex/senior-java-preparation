# JPA & Hibernate — Senior Interview Notes

## Persistence Context
The Persistence Context is a first-level cache and unit-of-work context for managed entities.

Entity states:
- `TRANSIENT`: new object, not associated with persistence context.
- `MANAGED`: tracked; dirty checking applies.
- `DETACHED`: previously managed but no longer tracked.
- `REMOVED`: scheduled for deletion.

## `persist`, `merge`, `remove`, `detach`
- `persist(entity)`: makes a new entity managed.
- `merge(entity)`: copies state into a managed instance and returns that managed instance; the argument itself stays detached.
- `remove(entity)`: marks managed entity for deletion.
- `detach(entity)`: stops tracking it.

## Dirty checking
Hibernate compares managed entity state and generates SQL at flush when state changed.

```java
@Transactional
public void rename(Long id, String name) {
    User user = em.find(User.class, id);
    user.setName(name); // no explicit save required for managed entity
}
```

## Flush vs commit
- `flush()`: synchronize persistence-context changes to DB with SQL.
- `commit()`: complete transaction and make changes durable.
- Flush can happen before commit and before queries depending on flush mode.
- A flushed change is still rolled back if transaction rolls back.

## Fetching
- `LAZY`: association loaded on access.
- `EAGER`: association requested eagerly; does not guarantee one SQL query.

Default rule: prefer LAZY associations and fetch explicitly according to use case.

### N+1 problem
One query loads N parents, then additional query per parent association.

Fixes:
- `JOIN FETCH`,
- `@EntityGraph`,
- projections,
- batch fetching where suitable.

Do not blindly fetch everything eagerly.

## `JOIN` vs `JOIN FETCH`
- JPQL `JOIN`: join for filtering/conditions, association may still remain lazy.
- `JOIN FETCH`: fetch associated entity/collection into persistence context.

`DISTINCT` is often used with collection fetch joins to avoid duplicate root entities at JPQL result level, but fetch-joining multiple collections can explode row counts.

## LazyInitializationException
Occurs when lazy data is accessed after the persistence context/session is unavailable.

Better solutions:
- fetch needed data inside service transaction,
- map to DTO while context is open,
- use explicit fetch plan.

Avoid solving everything with Open Session in View.

## Cascade and orphan removal
Cascade propagates entity operations (`PERSIST`, `MERGE`, `REMOVE`, etc.) from parent to child.

`orphanRemoval=true`: removing a child from the parent's association can delete that child row when it becomes an orphan.

Cascade REMOVE and orphan removal are related but not identical.

## Optimistic locking
```java
@Version
private long version;
```
Hibernate includes version in update condition. If another transaction already changed the row, update count becomes zero and an optimistic locking exception is thrown.

Good when conflicts are relatively rare.

## Pessimistic locking
Use DB row locks such as `SELECT ... FOR UPDATE` / `PESSIMISTIC_WRITE` when strong serialization for a row is needed.

Risks: blocking, deadlocks, reduced throughput.

## Bulk updates
JPQL bulk update/delete bypasses normal entity dirty checking and can leave Persistence Context stale.

With Spring Data:
```java
@Modifying(clearAutomatically = true)
@Query("update Order o set o.status = :status where o.id = :id")
int updateStatus(...);
```

## Pagination
- `Page<T>`: data + total count query.
- `Slice<T>`: data + whether next page exists; avoids total count.
- `List<T>`: no pagination metadata.

Large OFFSET pagination becomes increasingly expensive and can be unstable while rows change. Prefer keyset/cursor pagination for large ordered datasets.

## Common traps
- `merge()` does not attach the passed instance; it returns a managed copy.
- `flush()` is not commit.
- EAGER does not automatically solve N+1.
- LAZY collections should not be accessed arbitrarily outside persistence context.
- Bulk updates can make managed entities stale.

## Interview-ready summary
> I treat the Persistence Context as a unit of work: managed entities are dirty-checked and synchronized on flush. I keep associations lazy by default, use explicit fetch plans to avoid N+1, choose optimistic or pessimistic locking based on contention, and avoid large OFFSET pagination in high-scale systems.

## Review questions
1. `persist()` vs `merge()`?
2. Flush vs commit?
3. What causes N+1 and how do you fix it?
4. Cascade REMOVE vs orphanRemoval?
5. Page vs Slice?
6. Why can bulk JPQL make Persistence Context stale?
