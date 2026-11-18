# OTMS — Final Evaluation and Viva Preparation

This guide is based on implemented code and observed CI results as of **18 November 2026**. Review latest `main` before presenting. A role assigned in the project plan does **not** automatically mean a person wrote all the code in the corresponding feature.

## 60-second project explanation

OTMS coordinates operation-theatre schedules and hospital resources. Staff authenticate with JWT and use a React interface to list or create surgeries, detect overlapping theatre/surgeon/nurse bookings, verify equipment availability, view emergency impact, manually reschedule affected cases, read notifications, and export utilization reports. The application is backed by Spring Boot REST services, Spring Data JPA and Flyway migrations with H2 for the demo and PostgreSQL available through the production profile.

## Architecture defense

- **React/Vite:** isolated screens for the hospital workflow; local Vite development proxy sends `/api` to Spring Boot.
- **Spring Boot + Spring Security:** JSON REST endpoints and role-based method guards. Server-side authorization is required even if the UI hides a button.
- **JWT:** bearer token in each authenticated request. This is a prototype; localStorage token handling and long-lived dev secret are **not production hardening**.
- **Spring Data JPA transactions:** consistent surgery/resource writes.
- **Pessimistic row locks:** prevent parallel schedulers from confirming the same theatre, surgeon, nurse, or equipment time window.
- **Flyway migrations:** V1 core tables, V2 equipment/notifications, V3 nurse assignment.
- **H2 vs PostgreSQL:** local demo uses in-memory H2; PostgreSQL integration tests are run in CI. Not a claim of a deployed high-availability system.

## API questions and prepared answers

**What creates surgery?** `POST /api/surgeries` (ADMIN/COORDINATOR) with patient name, procedure, theatre ID, surgeon ID, nurse ID and start/end times; the server validates and checks active-resource overlap. A successful creation returns **201**. A clash returns **409** with structured error details.

**How do emergencies work?** `GET /api/surgeries/emergencies` lists active emergencies; `GET /api/surgeries/{id}/impact` finds non-emergency cases sharing time and resource constraints; `POST /api/surgeries/{id}/reschedule` validates the new slot and moves the affected surgery. It does **not** automatically optimize all schedules.

**How are theatre statuses changed?** `PATCH /api/theatres/{id}/status` accepts `{"active":false}` for ADMIN/COORDINATOR. Equipment uses `PATCH /api/equipment/{id}/status` with `{"available":false}` for ADMIN/TECHNICIAN.

**Which design decisions are incomplete?** Dedicated audit events/query, refresh-token/revoke/lockout/rate limiting, full shift/leave roster, per-recipient SMS/email and deployed uptime monitoring are not in current `main`.

## Team-role talking points (confirm actual PR participation)

| Project responsibility | Topics to rehearse | Evidence / distinction |
|---|---|---|
| Suravi — development lead | Spring Boot architecture, JWT, CI, React foundation, migration and integration decisions | [PR #2](../pull/2), [#3](../pull/3), [#4](../pull/4), automation and documentation; cite actual commit authors when discussing contributions |
| Shesanth — product owner | Equipment resource constraints, PostgreSQL/Flyway, RBAC, report exports, surgery edit | Issues #8, #11, #15, #19, #20 and merged feature PRs |
| Sudhiksha — QA lead | Test-case traceability, Maven/Vitest, API/Postman/Newman, branch coverage, performance, E2E risk | Issues #9–#13, #21 and QA checklists; **do not claim unmerged security/audit PRs are running in main** |
| Praneetha — OT coordinator | Nurse scheduling rules, theatre availability, notifications, emergency priority, operational acceptance scenarios | Issues #14, #16–#18, #30 and operational checklists; actual PR authorship needs GitHub verification |

During viva, each member should open their **own specific PR(s)** to explain what they authored, tested, reviewed or approved, rather than relying exclusively on issue assignment or retrospective commit attribution.

## Agile / SDLC questions

**What were the sprint goals?** Sprint 1 established backend/API and collaboration workflow; Sprint 2 integrated frontend, reporting and resource/emergency functionality; optional Sprint 3 addresses integration spillover, test updates and quality evidence. The mapping in `docs/sprint-plan-and-deviations.md` is retrospective—verify actual board iterations.

**How were points estimated?** Relative Fibonacci-style estimates (2/3/5/8), not elapsed hours or counts of commits. Overlapping legacy issues were excluded from double-counted velocity. See `docs/story-points-2026-11.md`.

**What is your test strategy?** Backend Maven/Spring tests, React/Vitest component tests, PostgreSQL/Flyway integration, real HTTP Newman checks, a normal-load performance probe and a **passing** 4-check Chromium browser smoke workflow. Manual usability and deployment-level NFR testing need separate acceptance.

## Measured test results and where to demonstrate

- [Main CI run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242): **65 backend tests** and **31 frontend tests** passed. PostgreSQL + Flyway integration passed.
- JaCoCo backend: **84.93% line**, **71.54% branch**.
- Vitest/V8 frontend: **79.64% line**, **76.34% branch**.
- [Newman/performance run #44213680844](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44213680844): **33 HTTP requests**, **36 assertions**, **zero failures**. Normal-load probe: **180 requests**, **p90 32.7 ms**, **zero failures**.
- [Browser E2E smoke run #44215079683](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44215079683): 4 checks passed (login, navigation, emergency impact, CSV download).
- The preceding measurements belong to **18 November 2026**, *not* the assignment's 2–6 November testing window.

## Final-demo checklist

- Show role login, theatre/surgery scheduling, the **409** conflict response, emergency impact and an available reschedule, a notification, and exported reports.
- Use only seeded **fictional** patient data. Keep JWTs and secrets out of recordings.
- Be ready to distinguish CI evidence from manual runs, historical sprint test results from current ones, and working prototype capabilities from production-grade requirements.
- Two submission videos remain planned separately: Sprint 1 terminal-only (~1 min); Sprint 2 full React UI/results (~2 min).
