# OTMS sprint plan, spillover, freeze gate and SAD/SRS deviations

**Updated:** 18 November 2026. This is a *retrospective reconstruction* from existing issue/PR history and a forward-looking closure plan. It **does not claim** the team originally scheduled every item into these exact sprints. Use the GitHub Project iteration fields as the authoritative planning record after checking them.

## Suggested sprint scope mapping (verify on GitHub Project)

| Phase | Focus | Evidence / examples | Exit criterion |
|---|---|---|---|
| Sprint 1 — backend foundation | Repository collaboration setup, Spring Boot, auth/JWT, core theatre/surgery scheduling and conflict checks | [PR #2](../pull/2), [PR #3](../pull/3), seed data and Maven tests | Backend starts, authentication returns JWT, surgery API retrieves data; 1-minute terminal video pending |
| Sprint 2 — integrated application | React UI, reports, DB migrations, role guards, resource & emergency scheduling, notifications | [PR #4](../pull/4), [#32](../pull/32), [#35](../pull/35), [#37](../pull/37), [#38](../pull/38), [#41](../pull/41), [#42](../pull/42), [#43](../pull/43), [#44](../pull/44), [#55](../pull/55), [#56](../pull/56) | UI/login and major operations run together; 2-minute UI video pending |
| Optional Sprint 3 — quality/integration spillover | Current STP updates, system/API regression, metrics, performance/concurrency evidence, security/audit/E2E gaps | [PR #58](../pull/58) and issues #9–#13, #21–#22, #30–#31 | Tests/reports complete, known gaps accepted or fixed, CI green and freeze signed |

This scope map is descriptive, not proof that each listed PR was completed inside the named calendar period. Backlog points are estimates in `docs/story-points-2026-11.md`; overlapping legacy tickets must not be counted twice as completed velocity.

## Actual architecture / requirements deviations

| Requirement or early plan | Actual implementation | Impact / disposition |
|---|---|---|
| MySQL or PostgreSQL relational storage | H2 in-memory is default for demos/tests; PostgreSQL supported via `prod`, Flyway V1–V3 | Local data resets on restart; production requires configured database |
| Full nurse/staff availability | Nurse assignment + conflicts against concurrent active surgery; no working shifts or leave-calendar model | SRS staff availability remains partially implemented; not a roster product |
| Theatre availability managed by technician/admin in earlier STP | Current backend `PATCH /api/theatres/{id}/status` allows ADMIN/COORDINATOR | Test cases and demo role expectations must match current policy or implementation should be consciously changed |
| Full audit logs | Notifications for scheduling events exist, but no dedicated persistent audit record/query API on main | Issue #13 open; cannot claim audit compliance |
| Robust authentication hardening | JWT login with role guards and development secret; refresh/revoke, throttling and lockout not merged | Issue #12 open; don't present as production hardened |
| Emergency rescheduling | Impact API + assisted manual reschedule, not an automatic optimizer | Demonstrate manual selection and revalidation; no false automatic claims |
| Notification recipients | Latest-50 in-app feed, mark-read support; no targeted SMS/email | Scope to in-app notifications |
| Reports | React-side date filtering, utilization aggregation, CSV and print | No separate reporting microservice |
| Deployment & uptime | Local Vite/Spring Boot dev; PostgreSQL CI integration; no proven 99.9% deployed availability | NFR evidence #22 outstanding |
| Testing scope | Maven/Vitest/CI and Postman collection; current UI E2E not automated | Add Newman CI + eventually browser E2E per issue #10 |

## Freeze decision record (pending team sign-off)

- **Candidate branch/commit:** `main`, currently `a983c69f9a3ac8c395f13503f8e4d3999652bc8e` at audit. Update to exact latest SHA before formal freeze.
- **Automatic gates:** Backend tests, frontend lint/tests/coverage, PostgreSQL migrations, docs checks, live Newman API validation, performance probe.
- **Manual gates:** Demo rehearsal, role-based usability pass, verified reports, any accepted security/audit scope deviations, teammate PR review, two final videos.
- **Decision:** **NOT FROZEN YET**. Do not record a fictitious historic freeze timestamp. Team owner can record a future freeze in a reviewed PR after all selected gates pass.

## Viva talking points

Why JWT: stateless authentication for HTTP APIs, with role guards for privileged changes. Why Spring Boot/JPA: transaction-scoped conflict validation. Why pessimistic locking: prevent simultaneous resource booking races. Why Flyway: consistent schema on H2 and PostgreSQL. Why Vitest/Maven/Postman: unit/component, service/integration, then HTTP acceptance layers. What is still weak: audit history, token lifecycle, manual usability/availability and true browser end-to-end testing.
