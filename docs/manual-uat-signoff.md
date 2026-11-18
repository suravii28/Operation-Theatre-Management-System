# OTMS — Manual User Acceptance Testing (UAT) record

**Prepared:** 18 November 2026 · **Release candidate:** `main` at `288a8b144d5f5e6d594395de38ebae696beb8e4c` immediately after the approved Part-3 PR #58 merged.

> This document is an **execution script and sign-off form**, not evidence that a human tester already ran the scenarios. All manual results below are marked **NOT EXECUTED** until a reviewer records them. Automated CI results are documented separately and should not be misrepresented as manual test execution.

## Pre-conditions

- Use a test machine with **Java 17**, supported Node version, a seeded local development backend and React/Vite application.
- Run `cd backend && ./mvnw spring-boot:run` and `cd frontend && npm ci && npm run dev` in separate terminals.
- Open `http://localhost:5173`; use fictional seeded patients and demo accounts from `backend/README.md`.
- Test as Coordinator for scheduling, Admin for permission checks, Nurse for read-only UI, and Technician for equipment control.
- If you need a deterministic fresh environment, restart the H2-backed backend before running the cases; note any local changes.
- Record each outcome with an evidence screenshot/video and exact date. No real patient information or JWTs in the evidence.

## Manual execution checklist

| ID | Role | Steps | Expected outcome | Actual result | Tester/date |
|---|---|---|---|---|---|
| UAT-01 | Coordinator | Log in and open dashboard | Dashboard loads, role/name visible; counts and today's schedule render | **NOT EXECUTED** | — |
| UAT-02 | Coordinator | Open Theatres and inspect occupied theatre | Active theatres list; bookings and status match seed | **NOT EXECUTED** | — |
| UAT-03 | Coordinator | Open Surgeries; select an available doctor, nurse, theatre and future slot; save | Booking succeeds and appears in list | **NOT EXECUTED** | — |
| UAT-04 | Coordinator | Try overlapping slot with the same doctor, theatre, or nurse | Conflict message; no duplicate surgery created (HTTP 409) | **NOT EXECUTED** | — |
| UAT-05 | Coordinator | Edit an upcoming surgery to a free slot | Updated time/theatre persists; conflicting edits rejected | **NOT EXECUTED** | — |
| UAT-06 | Coordinator | Open Emergencies, select seeded emergency, click Impact | Displaced routine case appears in impact list | **NOT EXECUTED** | — |
| UAT-07 | Coordinator | Reschedule displaced surgery to a verified free slot | New theatre/time saved, visible in schedule | **NOT EXECUTED** | — |
| UAT-08 | Coordinator | Toggle a theatre unavailable, then attempt new booking in it | Toggle succeeds, booking is rejected; restore availability | **NOT EXECUTED** | — |
| UAT-09 | Technician | Attempt theatre availability toggle | No permission to change theatre; control not offered / API responds 403 | **NOT EXECUTED** | — |
| UAT-10 | Technician | Change equipment maintenance status and restore it | Equipment flag updates for authorized technician | **NOT EXECUTED** | — |
| UAT-11 | Coordinator | Open Notifications and mark an unread item as read | Notification feed loads; unread count reduces after acknowledgement | **NOT EXECUTED** | — |
| UAT-12 | Coordinator | Open Reports; set a date range; export CSV, inspect file; open print view | Correct period, rows, CSV fields; browser print action works | **NOT EXECUTED** | — |
| UAT-13 | Nurse | Log in and attempt scheduling or privileged mutation | Restricted actions hidden or rejected by server | **NOT EXECUTED** | — |
| UAT-14 | Coordinator | Log out, attempt protected page without session | Redirect to login and reject unauthorized API access | **NOT EXECUTED** | — |
| UAT-15 | Team | Check keyboard navigation, layout at common widths, inline errors and readable feedback | Main tasks usable without broken layout; document limitations | **NOT EXECUTED** | — |

## Automated evidence already verified (not manual)

| Suite | Real run | Results |
|---|---|---|
| Main Maven/Vitest/PostgreSQL | [CI #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242) | 65/65 backend and 31/31 frontend tests passed |
| Newman live REST integration | [CI #44213680844](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44213680844) | 33/33 HTTP requests, 36/36 assertions, 0 failures |
| Performance | [CI #44213680844](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44213680844) | 180 requests, p90 32.7 ms, zero failures |
| Chromium UI smoke | [CI #44215079683](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44215079683) | Four browser smoke checks passed |

## Defect and closure record

| Defect | Severity | Reproduction | Owner / issue / PR | Fix or agreed deviation | Retest |
|---|---|---|---|---|---|
| _No manual defects have been recorded yet_ | — | — | — | — | — |

**Known scope limits:** No production uptime measurement; no dedicated audit-event API, complete shift roster, user-targeted notifications, or refresh-token/login-lockout system on `main`. These should be recorded as declared deviations, not falsely marked as manual test passes.

## UAT approval (blank until actual execution)

| Decision | Entry |
|---|---|
| Test environment and commit tested | _Pending_ |
| Tester name and date | _Pending_ |
| Passed cases / Failed cases / Blocked cases | _Pending_ |
| Screenshots or recorded evidence path | _Pending_ |
| Accepted deviations / remaining defects | _Pending_ |
| Product Owner sign-off | _Pending_ |
| QA Lead sign-off | _Pending_ |
| Final decision | **NOT SIGNED OFF** |

**Rule:** Do not enter PASS or sign on someone else's behalf. Attach actual outputs and signatures after the team performs the walkthrough.
