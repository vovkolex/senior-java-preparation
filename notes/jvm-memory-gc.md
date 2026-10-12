# JVM, Memory & GC — Senior Interview Notes

## Memory areas
- **Heap**: objects/arrays; shared between threads; managed by GC.
- **Thread stack**: one per thread; stack frames for method calls, local variables, operand stack.
- **Metaspace**: class metadata in native memory; replaced PermGen.
- Native/off-heap memory may also be used by direct buffers, threads, libraries, JVM internals.

## Stack vs Heap
- Deep/unbounded recursion can cause `StackOverflowError`.
- Allocation pressure/leaks can cause `OutOfMemoryError`.
- A Java memory leak means objects are still reachable but no longer useful, so GC cannot reclaim them.

## GC roots
Typical roots include:
- live thread stacks/local references,
- static references,
- JNI references,
- JVM/internal roots.

An object is collectible when it is no longer reachable from GC roots.

## Generational idea
Most objects die young, so collectors typically optimize for short-lived allocations.
- Young generation: Eden + survivor concepts.
- Long-lived objects may be promoted to old generation.
- Exact layout/behavior varies by collector.

## Shallow vs retained size
- **Shallow size**: memory of the object itself.
- **Retained size**: memory that could be freed if that object became unreachable, including exclusively retained objects.

Large retained size often helps identify leak dominators.

## Diagnosing memory issues
Useful signals/tools:
- heap dump,
- thread dump,
- GC logs,
- allocation profiling,
- JFR / Mission Control,
- container RSS vs heap usage,
- Metaspace/direct-buffer/thread counts.

### Heap leak approach
1. Confirm memory grows after GC.
2. Capture heap dump near high usage.
3. Inspect dominator tree / retained size.
4. Find unexpected path to GC root.
5. Fix ownership/lifecycle/caching/listener/thread-local problem.

## Common leak patterns
- unbounded maps/caches,
- static collections,
- listeners never deregistered,
- `ThreadLocal` values in thread pools,
- queues with producers faster than consumers,
- classloader leaks,
- holding large object graphs unintentionally.

## GC interview points
- GC reclaims unreachable memory; it does not fix reachable leaks.
- Stop-the-world pauses can happen even with concurrent collectors.
- Throughput, latency and memory footprint are trade-offs.
- Never tune GC before measuring allocation rate, pause distribution, heap occupancy and application latency.

## Interview-ready summary
> I distinguish heap pressure from native-memory issues, use GC-root reachability to reason about leaks, and diagnose with GC logs, heap dumps and JFR. A Java leak is usually a reachable-but-unneeded object graph, so the key is finding the retaining path rather than simply increasing heap size.

## Review questions
1. What is a GC root?
2. Why can a program leak memory even though Java has GC?
3. Shallow vs retained size?
4. Heap OOM vs Metaspace/native OOM?
5. What would you inspect if memory rises continuously after full GC?
