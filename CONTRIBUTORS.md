# Contributors and Work Distribution

All four team members have **write access** to this repository. Work is split
evenly by story points; every issue is owned by exactly one person, every
change lands through a reviewed pull request, and review duty rotates.

## Team

| Member | GitHub | Role | Owns |
|---|---|---|---|
| Suravi | [@suravii28](https://github.com/suravii28) | Development Lead | Workflow, CI, protection, merges, report pipeline |
| Sudhiksha | [@sudhikshavaranasi](https://github.com/sudhikshavaranasi) | QA Lead | Test strategy, STP test cases, audit trail, performance evidence |
| Praneetha | [@PraneethaSreedharala](https://github.com/PraneethaSreedharala) | OT Coordinator / Domain Rep | Resource availability, emergency handling, notifications |
| Shesanth | [@shestg28](https://github.com/shestg28) | Product Owner | Equipment, RBAC, deployment readiness, UX evidence |

## Issue ownership (equal split, ~19 story points each)

| Member | Issues | Points |
|---|---|---|
| Praneetha | #14 nurse availability, #16 emergency reschedule, #17 notifications, #18 theatre availability | 19 |
| Shesanth | #8 report export, #11 PostgreSQL/Flyway, #15 equipment, #19 RBAC guards, #20 surgery edit | 19 |
| Sudhiksha | #9 Postman suite, #10 E2E smoke tests, #12 security hardening, #13 audit logging, #21 performance/concurrency | 17 |
| Suravi | #23 report pipeline (infra) | 3 |

## How contributions happen

1. Claim your issue on the [project board](https://github.com/users/suravii28/projects/1).
2. Branch from `main`: `feature/<issue>-short-name`.
3. Code **plus tests** in the same PR — CI's test guard rejects PRs without them.
4. Push (local tests run automatically via the pre-push hook).
5. Open a PR using the template; request a review from a different member.
6. Merge happens only after CI is green and a review is approved.

Reviews are cross-assigned so no one reviews their own work:

| PR author | Typical reviewer |
|---|---|
| Praneetha | Shesanth |
| Shesanth | Sudhiksha |
| Sudhiksha | Praneetha |
| Suravi | Sudhiksha (QA sign-off) |
