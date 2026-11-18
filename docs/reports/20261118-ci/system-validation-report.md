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
| Full HTTP API/Postman collection against running backend | `docs/reports/system-validation.postman_collection.json` updated to 33 ordered requests | **PASS — 33 requests; 36 assertions; 0 failures (CI run 44213680844)** |
| Chromium browser E2E smoke | Four live UI checks; [run #44215079683](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44215079683) | **PASS (4/4), current PR branch only** |
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

## Confirmed live HTTP and performance execution — 18 Nov 2026

[GitHub Actions run #44213680844](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44213680844) **passed** with an actual Spring Boot dev server. Newman executed **33 requests**, **33 test scripts**, **12 pre-request scripts**, and **36 assertions**, with **zero request or assertion failures**. It covered login, role restrictions, assigned-nurse double-booking, unavailable theatre/equipment, emergency impact/reschedule, notifications, and report API data. The same run completed the **180-request** normal-load probe (6 concurrent workers) with **p90 = 32.7 ms**, **0 failures**, versus the **3000 ms** acceptance threshold.

Repository evidence: [redacted Newman run summary](system-validation-results-summary.json) and [machine-readable performance metrics](performance-results.json). The complete JSON execution output and backend logs are retained in GitHub Actions artifact **14387700936** (contains ephemeral local-demo tokens; do not publish raw logs outside the test team). The feature-branch run is not historical evidence for November 2–6 testing.

## Acceptance decision

**PARTIAL — backend/frontend/PostgreSQL CI, live Postman HTTP tests and the normal-load probe all passed.** Full browser E2E coverage (beyond the four passing smoke checks), signed manual usability walkthrough, deployed availability monitoring and dedicated security/audit tests remain outstanding. Do not label Part-3 2(a) fully complete until these are resolved or formally accepted as scope deviations.


**Supplementary browser evidence:** [Browser E2E smoke report](browser-e2e-smoke-report.md) documents 4 passing Playwright/Chromium checks and explicitly lists untested flows.
