# Java Core & Collections — Senior Interview Notes

## Core ideas

### `equals()` and `hashCode()`
- Equal objects must have the same `hashCode()`.
- Unequal objects may still have the same hash code: collisions are legal.
- Hash-based collections first use hash to choose a bucket, then `equals()` to identify the key.
- Mutable fields used by `equals/hashCode` make dangerous map/set keys: changing them after insertion can make the entry effectively unreachable.

### `HashMap`
- Average `get/put`: O(1); worst-case bucket lookup can become O(log n) after treeification.
- Capacity grows when `size > capacity * loadFactor` (default load factor 0.75).
- Treeification normally requires a sufficiently large bucket and table capacity (64+) before converting bucket list to a red-black tree.
- Allows one `null` key and multiple `null` values.
- Not thread-safe.

### `LinkedHashMap` / `TreeMap`
- `LinkedHashMap`: preserves insertion order, or access order when configured; useful for LRU-like caches.
- `TreeMap`: sorted by natural ordering or `Comparator`; typical operations O(log n).

### `ArrayList` vs `LinkedList`
- `ArrayList`: contiguous references, good CPU cache locality, O(1) random access, amortized O(1) append.
- `LinkedList`: O(n) random access, extra node/reference overhead; rarely better in real applications unless frequent operations already have a node/iterator position.

### `Set`
- `HashSet`: hash-based, no order guarantee.
- `LinkedHashSet`: insertion order.
- `TreeSet`: sorted, O(log n).

### Queue / Deque
- `add()` / `remove()` / `element()` throw on failure/empty.
- `offer()` / `poll()` / `peek()` return special value (`false`/`null`).
- Prefer `Deque` over legacy `Stack` for stack behavior.

## Complexity quick table

| Structure | Lookup | Insert | Ordered? |
|---|---:|---:|---|
| `ArrayList` index | O(1) | append amortized O(1) | insertion |
| `LinkedList` index | O(n) | O(1) at known node/end | insertion |
| `HashMap` | avg O(1) | avg O(1) | no |
| `TreeMap` | O(log n) | O(log n) | sorted |
| `HashSet` | avg O(1) | avg O(1) | no |

## Common traps
- `equals()` without matching `hashCode()` breaks hash collections.
- `HashMap` is not automatically ordered.
- `LinkedList` is not automatically faster for insertion; finding insertion position can still be O(n).
- `ConcurrentHashMap` is not equivalent to synchronizing a whole multi-step business invariant.

## Interview-ready summary
> Hash-based collections use the hash code to locate a bucket and equals to find the exact entry. I avoid mutable map keys, understand resizing/treeification trade-offs, and choose data structures based on access pattern rather than theoretical insertion cost alone.

## Review questions
1. What happens if a key's hash-relevant field changes after insertion into `HashMap`?
2. Why must equal objects have equal hash codes?
3. When does a HashMap bucket become a tree?
4. Why is `ArrayList` often faster than `LinkedList` in practice?
5. `offer/poll` vs `add/remove`?
