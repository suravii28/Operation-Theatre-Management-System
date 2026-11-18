# OTMS — actual remaining work and demo readiness

**Audit date:** 18 November 2026. Reviewed `main` source, 8 open issues, PR history and recent GitHub Actions runs. This is a **code/evidence audit**, not a claim of having exercised every UI workflow or deployed the app.

## Current health

- Latest observed `main` run ([CI run 44209143014](../../actions/runs/44209143014)) passed.
- PR [#55](../pull/55) (nurse availability) and [#56](../pull/56) (theatre availability) are **merged**. Issue #14 remains open despite the merged code.
- Draft PR [#53](../pull/53) (theatre date picker and notification filters) is **not merged**. Latest observed run [44209462158](../../actions/runs/44209462158) failed **frontend lint**; backend and PostgreSQL jobs passed. Lint reported a parsing error in `TheatresPage.jsx` and duplicate imports in `TheatresPage.test.jsx`. Do not merge as-is.
- The project plan was merged in [#54](../pull/54), but its completed/open story-point allocation predates #55/#56 and needs reconciliation.

## P0 — finish before recording the demo

| Priority | Real task | Evidence / acceptance |
|---|---|---|
| P0 | Demonstrate the **current merged build**, not draft #53 | `main` CI green; run local frontend and backend and execute the full presentation path once |
| P0 | Re-run API/system validation against the latest schema (V3 nurse) | Current `SchedulingService.schedule` rejects a request with no `nurseId`; legacy Postman requests/test-case docs may omit this field and thus no longer represent successful 201 booking |
| P0 | Validate theatre controls and roles | Current update is `PATCH /api/theatres/{id}/status`, restricted to ADMIN/COORDINATOR; older STP references `/availability` and ADMIN/TECHNICIAN |
| P0 | Correct outdated deliverables | `docs/reports/README.md` still says collection TODO/PRs pending; `docs/test-cases.md` describes old endpoints and test ownership. Regenerate dated report evidence after latest merges |
| P0 | Use only **fictional patient records** | Demo seed data is fictional; don't use real protected health information in screenshots, recordings or API examples |

## P1 — real implementation and test gaps

| Priority | Gap | Evidence / recommended next step |
|---|---|---|
| P1 | Security hardening (#12) | Current `AuthController` only logs in; `JwtService` only generates/parses a JWT. No implemented login rate limiting, failed-attempt lockout, refresh-token/revocation flow on `main`. [PR #50](../pull/50) was closed without merge. Implement/review with tests or explicitly mark out of scope for demo |
| P1 | Persistent audit logging (#13) | No `AuditEvent` entity/repository/controller in `main`; [PR #47](../pull/47) closed unmerged. Existing scheduling notifications are **not** an audit trail. Implement or document as incomplete |
| P1 | E2E test suite (#10) | Frontend has Vitest tests, but no Playwright/Cypress E2E suite or CI job exercising the running app end-to-end |
| P1 | API coverage and evidence (#9, #31) | Postman collection exists, but map all current endpoints and cases, update payloads for nurse assignment, and commit a fresh execution result |
| P1 | Performance and concurrency evidence (#21) | `scripts/perf-check.js` and `SchedulingConcurrencyTest` exist; the dedicated evidence PR [#51](../pull/51) was closed unmerged. Re-run load/concurrent-booking tests and save reproducible results |
| P1 | Nonfunctional reliability/usability evidence (#22) | No proof of 99.9% uptime or current usability study; add documented checks and honest scope |
| P1 | Surgery update doesn't apply requested equipment changes | `SchedulingService.update` updates surgeon, theatre, nurse and times, but does **not** apply or recheck `request.equipmentIds()` when editing; add update tests and implement proper equipment reconciliation |
| P1 | Status lifecycle transitions are permissive | `SchedulingService.updateStatus` sets the provided enum without checking allowed transitions (e.g. terminal-to-active). Add transition rules and negative tests |
| P1 | Reschedule nurse lock order | `reschedule` checks nurse conflict without explicitly locking the assigned nurse row, unlike `schedule`; assess concurrency correctness and add a race-condition test |

## P2 — polish / scope clarification

- Draft [PR #53](../pull/53) can be repaired (lint first, then tests) if date picker and notification filtering are desired. It is optional for the initial video.
- Staff `active` status is supported, but **shift rosters, leave calendars, and comprehensive availability hours** are not implemented. Scope the demo claim to conflict-based resource checks.
- Notifications are a global newest-50 list, not user-targeted email/SMS delivery. Do not claim per-user delivery.
- `README.md` links a missing `CONTRIBUTING.md` and contains literal `\\n` text in its PostgreSQL setup section. Fix presentation/docs before submission.
- `docs/test-cases.md` uses stale test authors/roles and has an outdated H2 `ddl-auto` claim; current code uses Flyway and `validate`.
- Current API docs and architecture diagram are in this PR for architecture and video narration.
- GitHub issue #14 and other open statuses should be reconciled **against delivered acceptance criteria** before marking closed.

## Demo-ready scope (once P0 checks pass)

1. Role-based login to the seeded local app.
2. Dashboard and current theatre occupancy.
3. Create surgery with surgeon, nurse, theatre, equipment and date.
4. Trigger a conflict (same surgeon, theatre or nurse) and show the **409** response/UI error.
5. Demonstrate emergency impact detection and assisted rescheduling.
6. Show change notification and read acknowledgement.
7. Export management reports (CSV/print view).
8. Optional: as ADMIN/COORDINATOR toggle theatre unavailable and show refusal to schedule into it.

## Decision rule

**Record the video only when the current `main` build runs locally, the planned scenario executes end-to-end, and the screenshots use fictional seeded data.** The latest `main` CI passing is encouraging, but does not alone demonstrate this live workflow.
