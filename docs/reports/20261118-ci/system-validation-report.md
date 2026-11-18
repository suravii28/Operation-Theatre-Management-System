# System Validation Report — current evidence and execution gap

**Evidence baseline:** GitHub `main` commit [`a983c69`](https://github.com/suravii28/Operation-Theatre-Management-System/commit/a983c69f9a3ac8c395f13503f8e4d3999652bc8e), [CI workflow run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), run on **18 November 2026 (UTC)**. These are CI results produced after the original **2–6 November** test window. They are *not* retroactively dated or claimed as that period's executions.

## Scope

System validation checks whether the integrated API and UI meet major SRS acceptance paths: login, authorization, surgery scheduling, double booking refusal, nurse/theatre/equipment resource checks, emergency impact/rescheduling, notification recording, and reports.

## Evidence status

| Check | Current evidence | Outcome |
|---|---|---|
| Spring Boot application context and Flyway V1–V3 | CI Maven/H2 + PostgreSQL/Flyway tests | **PASS in CI** |
| REST controller and scheduling service integration | 65 backend tests in CI | **PASS in CI** |
| React component and workflow behavior | 31 Vitest tests in CI | **PASS in CI** |
| PostgreSQL integration under `prod` profile | Dedicated CI PostgreSQL service job | **PASS in CI** |
| Full HTTP API/Postman collection against running backend | `docs/reports/system-validation.postman_collection.json` updated to 33 ordered requests | **NOT EXECUTED in this update** |
| Browser end-to-end (Playwright/Cypress) | No completed E2E CI suite in current `main` | **NOT VERIFIED** |
| Role-based manual walkthrough | No new signed execution record | **NOT VERIFIED** |
| Sustained availability and deployed-system validation | No deployed monitoring dataset | **NOT VERIFIED** |

**Do not interpret CI unit/integration success as a complete system acceptance sign-off.** The prior `docs/reports/20261031-sprint2/system-validation/results.json` belongs to an older code baseline and does not demonstrate the latest nurse assignment and theatre endpoint behavior.

## Updated test collection (Sprint-3 changes)

The 33-request Postman collection now:

1. Discovers active nurse IDs using `GET /api/staff?role=NURSE` and includes `nurseId` for surgery creation.
2. Uses `PATCH /api/theatres/{id}/status` rather than retired `/availability`.
3. Uses an ADMIN bearer token for theatre changes; adds an explicit **403** check for a TECHNICIAN role.
4. Includes explicit TC-RES-02 nurse double-booking rejection across distinct surgeon and theatre; retains surgery creation, role guards, equipment maintenance, emergency impact and rescheduling, notifications, and reporting data.

### Exact execution procedure to generate an HTTP results artifact

```bash
# Terminal 1: start freshly seeded development backend
cd backend
./mvnw spring-boot:run

# Terminal 2: run from repo root against this local backend
npx newman run docs/reports/system-validation.postman_collection.json \
  --reporters cli,json \
  --reporter-json-export docs/reports/20261118-ci/system-validation-newman-results.json
```

Run against a **freshly started** backend (H2 seeding is automatic); the ordered Postman suite modifies availability, creates surgeries and reads notifications. Use the CI-compatible Java runtime, do not expose bearer tokens or passwords in submitted results, and inspect any failed assertions before reporting PASS. This file **does not claim** Newman was executed.

## Acceptance decision

**PARTIAL — system-level automated unit/integration checks passed, but live Postman and browser E2E execution remain outstanding.** A signed manual acceptance record and real Newman JSON/CLI summary are needed before Part-3 2(a) can be marked fully complete.
