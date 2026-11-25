# Learning Progress

Baseline: 2026-11-25. Reconstructed from available learning-chat history and reported results; not an exhaustive audit of every conversation or repository. First pass != mastery. Unknown means unverified, not failed.

## Topic register
| Topic | Status | Review / next action |
|---|---|---|
| Java Core / Collections | First pass | equals/hashCode contract vs collisions, mutable keys, HashMap treeification/capacity, comparator equality, collection complexity |
| JVM | First pass | Check heap/stack/shared references, GC and performance depth |
| Concurrency / JMM | First pass | Check happens-before, visibility vs atomicity, volatile, synchronized, CAS, locks, CHM compound operations/internals; verify executors/futures/virtual threads depth |
| Spring Core / Boot / REST | First pass | Targeted proxy, DI, lifecycle, auto-configuration and REST questions |
| Spring Transactions | First pass + practice | Self-invocation, propagation, isolation, checked/unchecked rollback, flush vs commit |
| JPA / Hibernate / Spring Data | First pass + practice | persist vs merge (original remains detached), entity states, flush/clear, LAZY outside context, N+1/JOIN FETCH, Page vs Slice, locking; Q21 Dirty Checking completion unconfirmed |
| SQL / PostgreSQL | First pass | Queries, indexes, execution plans and isolation practice |
| Spring Security | Partial | Resume end-to-end Google -> Auth Server -> JWT -> order-service flow; prior estimate 80–85%, not a current assessment |
| Kafka / Messaging | First pass | Practical outbox, delivery semantics, idempotency, retries/DLQ |
| Microservices | First pass | Scenario-based trade-offs and failure handling |
| Testing | Partial / practical | Repository integration tests, TransactionTemplate, rollback; broader testing depth unverified |
| System Design | Started | Order processing at 10,000 orders/min: requirements, components, API, data and failure handling |
| English Interview | Ongoing | Unaided spoken answers; technical accuracy and concise English |
| Docker/K8s/cloud/observability/CI | Practical exposure; interview coverage unverified | Targeted checks later |

## LeetCode
| Task | Status | Evidence / review |
|---|---|---|
| Two Sum | Completed | HashMap O(n) expected time; implementation reported in repo |
| Longest Substring Without Repeating Characters | Completed | Sliding window + last-index map; accepted result reported, Q1–Q9 discussed; revisit invariant, max(left, previous+1), complexity |
| 3Sum | Not started | Codex smoke test only; no learning credit |
Other historical algorithm exercises (prefix sums, Floyd cycle detection) were discussed; independent completion not established. Continue pattern practice, not automatic repetition of completed tasks.

## OrderFlow
- Java 21, Spring, PostgreSQL 17/Docker, Flyway, JPA configured; OSIV disabled.
- OF-001 Persistence Layer: implementation and three repository integration tests reported complete; transactional rollback tested; commit/push reported.
- Pending verification: Q21 Dirty Checking, mvn clean verify, OF-001 PR status. Check existing state before repeating anything. Do not assume PR is required if workflow says otherwise.
- OF-002 Service Layer & REST API: next stage; completion not confirmed.
- Later: Kafka/outbox, idempotency, retries/DLQ, architecture.

## Daily log
| Date | Track | Focused minutes | Result |
|---|---|---|---|
| 2026-11-24 | OrderFlow | Unknown | Daily block finished; OF-001 implementation/tests/push reported |
| 2026-11-24 | Other tracks | Unknown | Completion not confirmed; System Design deferred |
| 2026-11-25 | LeetCode | 60 | Two Sum / Longest Substring review and coding reported; tests/docs present per prior repo review |
| 2026-11-25 | AI setup | Unknown | IntelliJ Air/Codex, AGENTS.md, gitignore configured; not counted as technical-study minutes |
| 2026-11-25 | OrderFlow / Java / System Design / English | Unknown | No confirmed completion yet |

## Resume queue
1. OrderFlow: 60 min planned; verify outstanding OF-001 items, then OF-002 if ready. Stop at timebox.
2. Java Core / Interview: 30 min diagnostic, Collections and JMM; no hints initially.
3. System Design: 20 min order-processing requirements and components.
4. English: 10 min spoken Dirty Checking and volatile vs synchronized.
5. Next LeetCode session: new pattern/task selected by mentor; 3Sum remains unstarted.
Today: 60/180 focused minutes reported, 120 planned remaining; reconcile actual time before final daily close. AI setup time is separate. If late or fatigued, carry unfinished blocks forward explicitly rather than claim completion.

## Session report template
- Date / track:
- Focused minutes (or unknown):
- Completed work / evidence:
- Independent understanding / gaps:
- Pending tasks / next action:
- Tests, commit or PR (if applicable):
