# Concurrency & Java Memory Model — Senior Interview Notes

## Race conditions and atomicity
`count++` is not atomic: read → modify → write. `volatile` does not make compound operations atomic.

Use the right primitive for the invariant:
- `synchronized` for mutual exclusion + visibility,
- `AtomicInteger/AtomicLong` for lock-free atomic updates,
- `LongAdder` for high-contention counters where exact instantaneous value is less important,
- explicit locks when you need timed/interruptible acquisition or multiple conditions.

## `volatile`
Guarantees visibility and ordering around the volatile variable; it does not provide mutual exclusion.

A write to a volatile variable happens-before a subsequent read of that same variable.

Typical use: stop flags / publication of immutable state.

## `synchronized`
Provides:
- mutual exclusion,
- visibility through monitor happens-before rules.

Instance synchronized method locks `this`; static synchronized method locks the `Class` object.

## Happens-before essentials
Important relations:
- program order within a thread,
- unlock happens-before later lock on same monitor,
- volatile write happens-before later volatile read,
- thread start/join rules,
- transitivity.

If no happens-before relationship exists, another thread may observe stale or reordered state allowed by the JMM.

## CAS and atomics
Compare-And-Set updates only if current value equals expected value.

```java
while (true) {
    int current = value.get();
    int next = current + 1;
    if (value.compareAndSet(current, next)) break;
}
```

CAS avoids blocking but can spin under contention. ABA can matter for some algorithms.

## `ReentrantLock`
Advantages over basic `synchronized`:
- `tryLock()`,
- timed lock attempts,
- interruptible lock acquisition,
- multiple `Condition`s.

Always unlock in `finally`:

```java
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

## Deadlock
Classic requirements include circular lock dependency. Prevent by:
- global lock ordering,
- reducing nested locks,
- `tryLock` + timeout/backoff,
- avoiding external calls while locks are held.

Thread dump is a primary production diagnostic.

## Concurrent collections
- `ConcurrentHashMap` supports high-concurrency access.
- Compound business operations may still require atomic map APIs (`compute`, `putIfAbsent`) or external synchronization.
- `CopyOnWriteArrayList` fits read-heavy/write-rare workloads.
- Blocking queues are useful for producer/consumer coordination.

## Thread pools
Bound queues and executor sizes deliberately. Unbounded queues can hide overload and create memory/latency problems.

For blocking I/O, pool sizing differs from CPU-bound work. Measure queue depth, active threads, task duration and rejection rate.

## Common traps
- `volatile int count; count++` is still racy.
- `AtomicInteger` does not make a multi-object business invariant atomic.
- Thread-safe collection ≠ thread-safe workflow.
- Holding a Java lock during slow HTTP/DB calls increases contention.

## Interview-ready summary
> I use happens-before to reason about visibility, atomics/CAS for simple atomic state, locks for multi-step invariants, and I avoid holding locks across slow operations. For production incidents I inspect thread dumps, executor saturation and lock contention rather than assuming more threads will solve the problem.

## Review questions
1. Why does volatile not fix `count++`?
2. What happens-before relation does synchronized provide?
3. AtomicLong vs LongAdder?
4. How would you prevent a two-lock deadlock?
5. Why can an unbounded executor queue be dangerous?
