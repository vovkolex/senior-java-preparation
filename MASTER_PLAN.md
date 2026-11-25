# Senior Java Master Plan

Baseline: 2026-11-25. Continuation of the existing September–November plan, not a restart.

## Priorities and schedule
- P1: Find a Senior Java Developer job as quickly as possible.
- P2: Hands-on Team Lead. Tech Lead/Architect development is deferred.
- 180 minutes of focused study per day, five days per week. Record actual focused minutes, excluding interruptions.
- Typical daily allocation: OrderFlow 60–75 min, LeetCode 45–60 min, Java/interview 30 min, System Design 20 min, English 10 min. Adjust to total 180; real interviews take precedence. Do not let OrderFlow consume the entire day.
- Career work (CV, LinkedIn, applications, recruiters) remains in the Career chat; pursue it in parallel.

## Existing learning tracks
| Track | Scope |
|---|---|
| Java/Spring/Interview | Java Core, Collections, JVM, JMM, concurrency, Spring Core/Boot/REST, transactions, JPA, SQL, Security, Kafka, microservices, testing, performance and mock interviews |
| OrderFlow | Java 21, Spring, PostgreSQL, Flyway, JPA/Hibernate, tests, service/REST layer; then Kafka, transactional outbox, idempotency, retries/DLQ and architecture; Docker, observability and deployment as appropriate |
| LeetCode | Independent Java coding, pattern practice, tests, complexity and verbal explanation |
| System Design | Requirements, APIs, data model, components, scaling, consistency, failure handling; start with order processing |
| English | Short spoken technical answers and mock interviews |
| Career | CV, LinkedIn, applications, recruiters and vacancy-specific preparation |

Continue the existing sequence: consolidate persistence/service/REST; reinforce Kafka/outbox and distributed-system concepts; practice architecture/system design; revisit JVM performance, SQL plans, observability, Docker/Kubernetes/cloud and CI as interview needs dictate. Use PROGRESS.md to select the next task, not the original calendar dates. Do not restart completed first passes.

## Learning and completion rules
- Ask one interview question at a time in English; wait for an independent answer. Give progressive hints, then feedback and concise English corrections.
- First pass is not mastery. Verify understanding through unaided answers, code, tests or scenarios. Record gaps and revisit them with targeted questions.
- Timebox tasks; unfinished work carries forward. End each day with actual minutes, evidence, gaps and next actions.
- Keep interview readiness and job search ahead of polishing the portfolio.
- 3Sum was only an AI-tool smoke test; it is not learning progress.

## Shared workflow
ChatGPT is the primary mentor and plan coordinator; Codex in JetBrains Air (Medium, Read-only by default) reviews local code, tests and files; GitHub is the shared durable record. Read AGENTS.md, MASTER_PLAN.md and PROGRESS.md at session start. Pull/fetch current changes before relying on local copies. ChatGPT conversations and Codex sessions do not automatically synchronize.
At session end report date, track, actual minutes (or unknown), work done, evidence (test/commit/path or interview answer), gaps and next action. ChatGPT reconciles the report into PROGRESS.md; Codex can propose a patch. Apply changes only with explicit permission, review the diff, commit/push only with explicit permission, and pull the resulting changes before the next session. Avoid concurrent edits; reconcile conflicts without dropping evidence. Change MASTER_PLAN.md only for agreed strategic changes. Keep detailed notes in existing notes files. Never claim tests ran or work completed without evidence.
