# OTMS story-point allocation — 17 November 2026

This is a **retrospective estimate of work scope**, not hours, number of commits, proof of authorship, or a claim that every acceptance criterion has passed. The **owner** is the person assigned to the project area/issue in `CONTRIBUTORS.md`; a GitHub PR may have been submitted by someone else. Use Fibonacci-style points: **2** small checklist, **3** bounded test/infra improvement, **5** substantial feature/test suite, **8** broad cross-layer work.

## Summary

| Owner | Completed / merged scope | Open / not confirmed merged | Total allocated |
|---|---:|---:|---:|
| Suravi (@suravii28) | 32 | 0 | **32** |
| Shesanth (@shestg28) | 28 | 3 | **31** |
| Sudhiksha (@sudhikshavaranasi) | 5 | 29 | **34** |
| Praneetha (@PraneethaSreedharala) | 21 | 7 | **28** |
| **Team** | **86** | **39** | **125** |

The completed column reflects work present via merged PRs or documentation added in [PR #52](../pull/52), **not** validation that every test or acceptance criterion is satisfied. Open issue status may lag behind PR activity; review the individual acceptance criteria before closing issues. Do not represent the total allocated column as completed velocity.

## Suravi — 32 points

| Work | Points | Status / evidence |
|---|---:|---|
| CI, collaboration workflow, PR templates and test gates | 5 | Merged [PR #2](../pull/2) |
| Spring Boot backend, JWT auth and core scheduling | 8 | Merged [PR #3](../pull/3) |
| React frontend and initial workflow pages | 8 | Merged [PR #4](../pull/4) |
| Unit, coverage and system validation report pipeline | 3 | Closed [issue #23](../issues/23), [PR #25](../pull/25), [PR #26](../pull/26) |
| Contributor allocation and teammate onboarding | 3 | Merged [PR #24](../pull/24), [PR #27](../pull/27) |
| System validation fixtures and expanded test-case catalog | 5 | Merged [PR #39](../pull/39), [PR #40](../pull/40) |

## Shesanth — 31 points

| Work | Points | Status / evidence |
|---|---:|---|
| Reports CSV export, date filters and print view | 5 | Closed [#8](../issues/8), merged [PR #42](../pull/42) |
| PostgreSQL / Flyway migration and local deployment | 5 | Closed [#11](../issues/11), merged [PR #44](../pull/44) |
| Equipment records, scheduling availability checks | 8 | Closed [#15](../issues/15), merged [PR #41](../pull/41) |
| Role-based surgery endpoint authorization | 5 | Closed [#19](../issues/19), merged [PR #32](../pull/32) |
| Surgery editing with conflict revalidation | 5 | Closed [#20](../issues/20), merged [PR #43](../pull/43) |
| Availability and usability NFR evidence | 3 | **Open** [#22](../issues/22) — not counted as completed |

## Sudhiksha — 34 points

| Work | Points | Status / evidence |
|---|---:|---|
| Postman API coverage mapped to STP | 3 | **Open** [#9](../issues/9) |
| E2E smoke test coverage | 5 | **Open** [#10](../issues/10) |
| Rate limiting, account lockout and token hardening | 8 | **Open** [#12](../issues/12); [PR #50](../pull/50) closed without merge |
| Audit logging for auth and scheduling actions | 5 | **Open** [#13](../issues/13); [PR #47](../pull/47) closed without merge |
| Performance and concurrency test evidence | 5 | **Open** [#21](../issues/21); [PR #51](../pull/51) closed without merge |
| Extended system validation scenarios | 3 | **Open** [#31](../issues/31) |
| QA checklists, acceptance/release review notes (12 documents) | 5 | Merged [PR #52](../pull/52); documentation only, **not** evidence of executed tests |

## Praneetha — 28 points

| Work | Points | Status / evidence |
|---|---:|---|
| Nurse availability and conflict prevention | 5 | **Open** [#14](../issues/14); [PR #49](../pull/49) closed without merge |
| Emergency impact and assisted rescheduling | 8 | Closed [#16](../issues/16), merged [PR #37](../pull/37) |
| In-app schedule and emergency notifications | 5 | Closed [#17](../issues/17), merged [PR #38](../pull/38) |
| Theatre availability management | 3 | Closed [#18](../issues/18), merged [PR #35](../pull/35) |
| STP scenarios for availability and emergency flows | 2 | **Open** [#30](../issues/30) |
| Operations requirements and acceptance-review checklists (12 documents) | 5 | Merged [PR #52](../pull/52); documentation only, **not** implemented backend features |

## Exclusions and double-counting rules

- Legacy [#5](../issues/5), [#6](../issues/6), and [#7](../issues/7) overlap later, more specific resource/notification/audit issues. Their work is **not** counted twice.
- Migration extension [PR #45](../pull/45) is included in database/equipment scope, not a new allocation.
- The merged PR author and the owner listed here may differ. Story points indicate **planning ownership, not verified individual coding credit**.
- The two feature changes in **draft [PR #53](../pull/53)** (theatre date selection, notification filters) are **excluded** until reviewed and merged; reassess after tests pass.
- Update estimates during sprint review; don't infer contribution or productivity directly from commit counts.
