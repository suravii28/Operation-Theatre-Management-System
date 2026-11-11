# OTMS Test Case Specification

| | |
|---|---|
| **Document** | Test Case Specification (Deliverables Part-3 support document) |
| **Version** | 2.0 (Sprint-3) |
| **Date** | 2026-11-11 |
| **Authors** | OTMS Team — see STP §9 for roles (QA Lead: Praneetha; Test Engineer: Suravi; Developer: Sudhiksha) |
| **Status** | Approved for execution |
| **Baseline** | SRS v1.0, SAD v1.0, STP v1.0 (all in the repository root) |

This document is the single source of truth for OTMS test cases: every case
maps to an SRS requirement, states its preconditions, steps and expected
result, names the automated implementation that executes it (if any), and
records its current status. Execution results and evidence live in
[docs/reports/](reports/README.md).

## Version history

| Version | Date | Change |
|---|---|---|
| 1.0 | 2026-10-31 | Sprint-2 baseline: 14 backend + 10 frontend automated tests and a 9-request Postman collection. Catalog maintained informally in `STP_OTMS.docx` §13. |
| 2.0 | 2026-11-11 | Sprint-3: this document. New/automated cases for role guards (TC-Auth-03/04, TC-SEC-02), theatre availability toggle (TC-RES-04), emergency impact & reschedule (TC-EMG-02/03/04), notifications (TC-NOT-01/02), concurrency (TC-Conc-01) and performance (TC-Perf-01). Postman collection extended 9 → 23 requests; `scripts/perf-check.js` added; suite now 45 backend + 26 frontend automated tests. |

## How to execute the suite

| Suite | Command | What it covers |
|---|---|---|
| Backend unit/integration | `cd backend && ./mvnw test` (Java 17) | Service, controller and security cases; JaCoCo coverage report in `backend/target/site/jacoco` |
| Frontend unit | `cd frontend && npm test` (Node 18+) | Page/component and pure-logic cases |
| System validation (API) | Start the backend (`./mvnw spring-boot:run`), then `npx newman run docs/reports/system-validation.postman_collection.json` | End-to-end acceptance paths against a freshly seeded instance |
| Performance | `node scripts/perf-check.js` (against the running backend) | OTMS-NF-001 response-time budget |

Notes:

- The backend uses an in-memory H2 database that is **reseeded on every
  boot** by `DataInitializer`. The Postman collection assumes a freshly
  booted instance; restart the backend before re-running it.
- CI (`.github/workflows/ci-cd.yml`) runs the backend and frontend suites on
  every pull request; the required `CI Success` check blocks merges when a
  test fails (rules 3, 7, 9).
- All patient, staff and surgery data is fictional test data.

## Test environment

- Java 17, Spring Boot 3.3, H2 in-memory database (`ddl-auto: create-drop`).
- Node 18+, React + Vite + Vitest frontend.
- Seed accounts (password shown for the local/test environment only):

| Account | Role |
|---|---|
| `admin@otms.local` / `Admin123!` | ADMIN |
| `coordinator@otms.local` / `Coord123!` | COORDINATOR |
| `dr.ram@otms.local` / `Doctor123!` | DOCTOR |
| `dr.priya@otms.local` / `Doctor123!` | DOCTOR |
| `nurse@otms.local` / `Nurse123!` | NURSE |
| `tech@otms.local` / `Tech123!` | TECHNICIAN |

---

## 1. Authentication & Access Control

### TC-Auth-01 — Login with valid credentials
- **Requirement:** OTMS-F-001 · Priority: High
- **Preconditions:** Backend running with seed data; account exists.
- **Steps:** `POST /api/auth/login` with valid admin credentials.
- **Expected:** `200` with a non-empty JWT token plus the user's name and role.
- **Automated by:** `AuthControllerTest.loginReturnsTokenForValidCredentials`; Postman `TC-Auth-01`.
- **Status:** Automated — PASS.

### TC-Auth-02 — Login with invalid credentials rejected
- **Requirement:** OTMS-F-001 · Priority: High
- **Preconditions:** Backend running.
- **Steps:** `POST /api/auth/login` with a wrong password; separately, a malformed body.
- **Expected:** `401` for bad credentials, `400` for a malformed request; no token issued.
- **Automated by:** `AuthControllerTest.loginRejectsInvalidCredentials`, `AuthControllerTest.loginRejectsMalformedRequest`; Postman `TC-Auth-02`.
- **Status:** Automated — PASS.

### TC-Auth-03 — Insufficient role denied on protected endpoints (RBAC)
- **Requirement:** OTMS-F-002 / OTMS-SR-002 · Priority: High · *Added in v2.0*
- **Preconditions:** Authenticated session with a role that lacks the required permission.
- **Steps:** As DOCTOR: `POST /api/surgeries` and `POST /api/surgeries/{id}/reschedule` with otherwise-valid payloads. As NURSE: `PATCH /api/surgeries/{id}/status` and `PATCH /api/theatres/{id}/availability`.
- **Expected:** `403` with a role-denial message; no data modified.
- **Automated by:** `SurgeryControllerSecurityTest.doctorCannotCreateSurgery`, `.doctorCannotRescheduleSurgery`, `.nurseCannotChangeSurgeryStatus`; `TheatreControllerTest.nurseCannotToggleTheatreAvailability`; Postman `TC-SEC-02` (doctor login + two 403 probes).
- **Status:** Automated — PASS.

### TC-Auth-04 — UI hides actions unavailable to the role
- **Requirement:** OTMS-F-002 · Priority: Medium · *Added in v2.0*
- **Preconditions:** Logged in as NURSE on the Theatres page.
- **Steps:** Render the Theatres page as a nurse.
- **Expected:** No availability-toggle buttons are shown and only active theatres are requested.
- **Automated by:** `TheatresPage.test.jsx` — "hides the toggle for nurses and requests active theatres only".
- **Status:** Automated — PASS.

## 2. Surgery Management

### TC-SURG-01 — Create a valid surgery record
- **Requirement:** OTMS-F-004 · Priority: High
- **Preconditions:** ADMIN/COORDINATOR token; free theatre slot; active surgeon.
- **Steps:** `POST /api/surgeries` with patient, procedure, theatre, surgeon, start/end times.
- **Expected:** `201` with the created record in `SCHEDULED` status. Negative variant: payload with blank fields or end-before-start returns `400`.
- **Automated by:** `SchedulingServiceTest.schedulesSurgeryWhenNoConflict`; `SurgeryControllerSecurityTest.adminCanCreateSurgery`, `.invalidSurgeryPayloadIsRejected`; Postman `TC-SURG-01`.
- **Status:** Automated — PASS.

### TC-SURG-02 — Update surgery status
- **Requirement:** OTMS-F-005 · Priority: High
- **Preconditions:** A surgery in `SCHEDULED` status exists.
- **Steps:** `PATCH /api/surgeries/{id}/status` with `{"status":"IN_PROGRESS"}` (as ADMIN, COORDINATOR or DOCTOR).
- **Expected:** `200` with the new status; a status-change notification is recorded.
- **Automated by:** `SchedulingServiceTest.statusChangeRecordsANotification`; `SurgeryControllerSecurityTest.doctorCanChangeSurgeryStatus`; Postman `TC-SURG-02`.
- **Status:** Automated — PASS.

### TC-SURG-03 — View surgery list and details
- **Requirement:** OTMS-F-006 · Priority: High
- **Preconditions:** Valid token; at least one seeded surgery.
- **Steps:** `GET /api/surgeries` (optionally with `date`, `theatreId`, `status` filters); `GET /api/surgeries/{id}`.
- **Expected:** `200` with an array of surgery records carrying patient, procedure, surgeon, theatre, times, status and emergency fields.
- **Automated by:** `SurgeryControllerSecurityTest.authenticatedUserCanListSurgeries`; Postman `TC-SURG-03` and `TC-REP-01`.
- **Status:** Automated — PASS.

## 3. Scheduling

### TC-SCH-01 — Schedule surgery into a free slot
- **Requirement:** OTMS-F-007 · Priority: High
- **Preconditions:** Active theatre and surgeon with no overlapping bookings in the target window.
- **Steps:** Create a surgery in a free slot; separately create a back-to-back booking that shares an edge time.
- **Expected:** Both succeed; conflict check treats adjacent (non-overlapping) windows as free.
- **Automated by:** `SchedulingServiceTest.schedulesSurgeryWhenNoConflict`, `.allowsNonOverlappingSurgeryInSameTheatre`; frontend `conflicts.test.js` — "treats back-to-back bookings as non-conflicting".
- **Status:** Automated — PASS.

### TC-SCH-02 — Theatre availability listing
- **Requirement:** OTMS-F-008 · Priority: High
- **Preconditions:** At least one active and one inactive theatre exist.
- **Steps:** `GET /api/theatres` (default) and `GET /api/theatres?includeInactive=true`.
- **Expected:** Default list excludes inactive theatres; `includeInactive` lists all.
- **Automated by:** `TheatreControllerTest.listsActiveTheatresByDefault`, `.includesInactiveTheatresWhenRequested`; Postman setup request and the `TC-RES-04` chain.
- **Status:** Automated — PASS.

### TC-SCH-03 — Overlapping surgery in the same theatre rejected
- **Requirement:** OTMS-F-009 · Priority: High
- **Preconditions:** A surgery occupies OT-1 today 09:00–11:00 (seeded).
- **Steps:** Attempt to schedule another surgery in OT-1 overlapping that window.
- **Expected:** `409` with a conflict message naming the occupied slot.
- **Automated by:** `SchedulingServiceTest.rejectsOverlappingSurgeryInSameTheatre`; `SurgeryControllerSecurityTest.schedulingConflictReturns409`; Postman `TC-SCH-02/03`.
- **Status:** Automated — PASS.

## 4. Resource Availability

### TC-RES-01 — Surgeon double-booking rejected
- **Requirement:** OTMS-F-010 · Priority: High
- **Preconditions:** Dr. Ram Sharma is booked today 09:00–11:00 (seeded).
- **Steps:** Attempt to schedule the same surgeon in a *different* theatre overlapping that window.
- **Expected:** `409` — a surgeon cannot be in two theatres at once.
- **Automated by:** `SchedulingServiceTest.rejectsOverlappingSurgeryForSameSurgeonInDifferentTheatre`; frontend `conflicts.test.js` — "flags the same lead surgeon booked in different theatres"; Postman `TC-RES-01`.
- **Status:** Automated — PASS.

### TC-RES-02 — Nurse availability
- **Requirement:** OTMS-F-011 · Priority: Medium
- **Status:** **Deferred** — nurse roster/availability is not part of the current data model; tracked in issue #14.

### TC-RES-03 — Equipment availability
- **Requirement:** OTMS-F-012 · Priority: Medium
- **Status:** **Deferred** — equipment module not yet implemented; tracked in issue #15.

### TC-RES-04 — Theatre availability toggle *(new in v2.0)*
- **Requirement:** OTMS-F-021 · Priority: High
- **Preconditions:** TECHNICIAN or ADMIN logged in; an active theatre exists.
- **Steps:** `PATCH /api/theatres/{id}/availability` with `{"active":false}`; attempt to schedule into the disabled theatre; toggle back to `{"active":true}`. Negative variants: missing `active` field (`400`), unknown theatre (`404`), NURSE attempting the toggle (`403`).
- **Expected:** Toggle returns the theatre with the new state; scheduling into an inactive theatre is rejected with `404`; after re-enabling, scheduling succeeds; the UI shows the toggle only to technicians/admins.
- **Automated by:** `TheatreControllerTest` (6 tests); `SchedulingServiceTest.rejectsSchedulingInInactiveTheatre`; `TheatresPage.test.jsx` (4 tests); Postman `TC-RES-04` chain (disable → 404 → re-enable).
- **Status:** Automated — PASS.

### TC-RES-05 — Equipment maintenance status
- **Requirement:** OTMS-F-022 · Priority: Medium
- **Status:** **Deferred** — equipment module not yet implemented; tracked in issue #15.

## 5. Conflict Detection

### TC-CON-01 — Scheduling conflicts detected
- **Requirement:** OTMS-F-013 · Priority: High
- **Preconditions:** Seeded double-booking scenario exists (Dr. Ram in OT-1 and OT-2 today).
- **Steps:** Compute conflicts over the surgery list (theatre overlap and surgeon overlap); verify cancelled/completed surgeries are ignored.
- **Expected:** Overlaps are flagged; non-overlapping and non-active records are not.
- **Automated by:** frontend `conflicts.test.js` (5 tests); `SchedulingServiceTest.conflictCheckOnlyConsidersActiveStatuses`.
- **Status:** Automated — PASS.

### TC-CON-02 — Conflict details surfaced to the user
- **Requirement:** OTMS-F-014 · Priority: Medium
- **Preconditions:** A conflicting scheduling attempt is made.
- **Steps:** Read the `409` response body; view the surgery list in the UI.
- **Expected:** The error message names the theatre, patient, surgeon and occupied window; the UI shows a conflict badge for double-booked surgeons.
- **Automated by:** `SurgeryControllerSecurityTest.schedulingConflictReturns409` (message assertions); `ConflictBadge.test.jsx`; Postman `TC-SCH-02/03` (message assertion).
- **Status:** Automated — PASS.

## 6. Emergency Handling

### TC-EMG-01 — Emergency surgeries are listed
- **Requirement:** OTMS-F-015 · Priority: High
- **Preconditions:** At least one emergency surgery seeded (`Lakshmi Rao`, emergency laparotomy).
- **Steps:** `GET /api/surgeries/emergencies`.
- **Expected:** `200`; every returned record has `emergency: true`; the seeded emergency is present.
- **Automated by:** Postman `TC-EMG-01`; `EmergenciesPage.test.jsx` — "shows the empty state…".
- **Status:** Automated — PASS.

### TC-EMG-02 — Emergency prioritization displaces routine surgery
- **Requirement:** OTMS-F-016 · Priority: High · *Automated in v2.0*
- **Preconditions:** Seed scenario: routine surgery (Mohan Verma) and an emergency (Lakshmi Rao) share OT-1 two days out at 15:00–17:00.
- **Steps:** Inspect the seeded scenario; run impact analysis for the emergency.
- **Expected:** The emergency case is recorded in the same slot as the routine case (prioritised), and the routine case is reported as displaced.
- **Automated by:** `DataInitializerTest.seededEmergencyHasADeterministicDisplacedSurgery` (guards the seed in CI); exercised end-to-end by Postman `TC-EMG-03`.
- **Status:** Automated — PASS.

### TC-EMG-03 — Impact analysis identifies affected surgeries *(new in v2.0)*
- **Requirement:** OTMS-F-017 · Priority: High
- **Preconditions:** An emergency surgery exists; a routine surgery overlaps it in theatre or surgeon.
- **Steps:** `GET /api/surgeries/{emergencyId}/impact`. Negative variant: call it for a non-emergency id.
- **Expected:** `200` with the displaced non-emergency surgeries (the emergency itself and other emergencies excluded); non-emergency input rejected with `400`.
- **Automated by:** `SchedulingServiceTest.impactAnalysisFindsDisplacedNonEmergencySurgeries`, `.impactAnalysisExcludesItselfAndOtherEmergencies`, `.impactAnalysisRejectsNonEmergencySurgery`; `SurgeryControllerSecurityTest.anyAuthenticatedRoleCanViewEmergencyImpact`; `EmergenciesPage.test.jsx` (impact panel); Postman `TC-EMG-03`.
- **Status:** Automated — PASS.

### TC-EMG-04 — Reschedule the affected surgery *(new in v2.0)*
- **Requirement:** OTMS-F-018 · Priority: High
- **Preconditions:** A displaced surgery exists (from TC-EMG-03); a conflict-free alternative slot is known.
- **Steps:** `POST /api/surgeries/{id}/reschedule` with a new theatre/time. Negative variants: new slot conflicts (`409`), end-before-start (`400`), DOCTOR role (`403`).
- **Expected:** `200` with the surgery moved to the new theatre and slot, still `SCHEDULED`; a reschedule notification is recorded.
- **Automated by:** `SchedulingServiceTest.rescheduleMovesSurgeryToConflictFreeSlot`, `.rescheduleRejectedWhenNewSlotConflicts`, `.rescheduleRejectedWhenEndBeforeStart`; `SurgeryControllerSecurityTest.coordinatorCanRescheduleSurgery`, `.doctorCannotRescheduleSurgery`; `EmergenciesPage.test.jsx` (reschedule form); Postman `TC-EMG-04`.
- **Status:** Automated — PASS.

## 7. Notifications

### TC-NOT-01 — Schedule-change notifications *(new in v2.0)*
- **Requirement:** OTMS-F-019 · Priority: High
- **Preconditions:** A schedule, status-change or reschedule event has occurred.
- **Steps:** `GET /api/notifications`; `PATCH /api/notifications/{id}/read`.
- **Expected:** `200` with a feed of typed notifications (`SURGERY_SCHEDULED`, `SURGERY_STATUS_CHANGED`, `SURGERY_RESCHEDULED`) carrying message, surgery id, read flag; mark-read returns `read: true`; unknown id returns `404`; the endpoint requires authentication.
- **Automated by:** `SchedulingServiceTest.schedulingRecordsANotificationForTheNewSurgery`, `.statusChangeRecordsANotification`, `.reschedulingRecordsARescheduleNotification`; `NotificationControllerTest` (4 tests); `NotificationsPage.test.jsx` (4 tests); Postman `TC-NOT-01`, `TC-NOT-02`.
- **Status:** Automated — PASS.

### TC-NOT-02 — Emergency notifications *(new in v2.0)*
- **Requirement:** OTMS-F-020 · Priority: High
- **Preconditions:** An emergency surgery is scheduled.
- **Steps:** Schedule an emergency; read the notification feed.
- **Expected:** An `EMERGENCY_SCHEDULED` notification appears in the feed and the UI renders it.
- **Automated by:** `SchedulingServiceTest.emergencySchedulingRecordsAnEmergencyNotification`; `NotificationsPage.test.jsx` — "lists schedule-change and emergency notifications"; Postman `TC-NOT-01` (feed includes emergency/reschedule types).
- **Status:** Automated — PASS. *(Role-targeted delivery — e.g. notifying only the affected surgeon — is documented as future work; the current feed is visible to all authenticated roles.)*

## 8. Reporting

### TC-REP-01 — Utilisation report reflects recorded schedules
- **Requirement:** OTMS-F-023 · Priority: Medium
- **Preconditions:** Surgeries exist across multiple theatres.
- **Steps:** Open the Reports page; query the surgery list with a date filter.
- **Expected:** The report aggregates surgeries and booked hours per theatre and counts per status, matching the underlying records; newly created surgeries appear in the aggregates.
- **Automated by:** `ReportsPage.test.jsx` (3 tests — aggregation, empty state, error); Postman `TC-REP-01` (report data fields + created surgery reflected).
- **Status:** Automated — PASS.

## 9. Security

### TC-SEC-01 — TLS 1.2+ transport
- **Requirement:** OTMS-SR-001 · Priority: High
- **Status:** **Deferred** — TLS is terminated at the production reverse proxy; local dev/test runs plain HTTP on localhost. Execute at deployment time (production checklist).
- **Planned check:** `curl -Iv https://<host>` reports TLS 1.2+; HSTS header present.

### TC-SEC-02 — Role-based access control on API operations *(automated in v2.0)*
- **Requirement:** OTMS-SR-002 · Priority: High
- **Steps:** Attempt role-restricted operations (`POST /api/surgeries`, `PATCH status`, `POST reschedule`, `PATCH theatre availability`) with roles outside the allowed set.
- **Expected:** `403` for every disallowed role; allowed roles succeed.
- **Automated by:** `SurgeryControllerSecurityTest` (11 tests), `TheatreControllerTest.nurseCannotToggleTheatreAvailability`; Postman `TC-SEC-02`.
- **Status:** Automated — PASS.

### TC-SEC-03 — Audit logging
- **Requirement:** OTMS-SR-003 · Priority: High
- **Status:** **Deferred** — audit logging is not yet implemented; tracked in issue #13.

### TC-SEC-04 — Unauthenticated access denied
- **Requirement:** OTMS-SR-004 · Priority: High
- **Steps:** Call protected endpoints (`/api/surgeries`, `/api/notifications`, …) without a token.
- **Expected:** `401`/`403`; no data returned.
- **Automated by:** `SurgeryControllerSecurityTest.surgeriesEndpointRequiresAuthentication`; `NotificationControllerTest.notificationsEndpointRequiresAuthentication`; Postman `TC-Sec-05`.
- **Status:** Automated — PASS.

### TC-SEC-05 — API request validation and authorization
- **Requirement:** OTMS-SR-005 · Priority: High
- **Steps:** Send malformed/invalid payloads (blank fields, bad JSON, end-before-start) to authenticated endpoints; send requests with an expired/tampered token.
- **Expected:** `400` for invalid payloads; `401`/`403` for invalid credentials; the API never partially applies an invalid request.
- **Automated by:** `SurgeryControllerSecurityTest.invalidSurgeryPayloadIsRejected`; `AuthControllerTest.loginRejectsMalformedRequest`; Postman `TC-Auth-02`, `TC-Sec-05`.
- **Status:** Automated — PASS.

### TC-SEC-06 — No credentials or sensitive data in logs
- **Requirement:** OTMS-SR-006 · Priority: High
- **Steps:** Exercise login and scheduling; review application logs.
- **Expected:** No passwords, tokens or patient identifiers appear in plaintext logs.
- **Status:** **Manual — PASS** (code review: `JwtAuthenticationFilter` and controllers log no credentials or payloads; logging level for prod profile is `WARN`). Repeat before release.

## 10. Non-Functional

### TC-Perf-01 — Response time within budget *(automated in v2.0)*
- **Requirement:** OTMS-NF-001 · Priority: High
- **Preconditions:** Backend freshly booted with seed data.
- **Steps:** `node scripts/perf-check.js` — mixed workload (surgery list, theatre list, scheduling writes) at concurrency 6, 180 requests.
- **Expected:** p90 ≤ 3000 ms (SRS acceptance: ≥ 90% of requests within 3 s).
- **Latest result (2026-11-11):** p90 = 20.6 ms, 0 failures — PASS (see `docs/reports/20261111-sprint3/`).
- **Status:** Automated — PASS.

### TC-Rel-01 — System availability
- **Requirement:** OTMS-NF-002 · Priority: Medium
- **Status:** **Manual/Deferred** — requires a deployed environment and uptime monitoring; not measurable in CI. Restart-recovery behaviour is covered implicitly: the H2 schema is rebuilt and reseeded on every boot.

### TC-UX-01 — Usability
- **Requirement:** OTMS-NF-005 · Priority: Medium
- **Steps:** Role-based walkthrough of the UI (login, schedule, toggle theatre, emergency impact/reschedule, notifications, reports).
- **Expected:** Each role can complete its primary tasks without training; errors are surfaced inline; navigation reflects permissions.
- **Status:** **Manual — PASS** (walkthrough during sprint-3 demo; permission gating additionally covered by `TheatresPage.test.jsx`).

### TC-Conc-01 — Concurrent scheduling integrity *(new in v2.0)*
- **Requirement:** OTMS-NF-006 · Priority: High
- **Preconditions:** One free theatre slot; multiple concurrent schedulers.
- **Steps:** 6 threads × 5 rounds all attempt to book the same slot simultaneously (start-gun synchronised).
- **Expected:** Exactly one booking per round wins; the rest receive `409`; the database is never double-booked (pessimistic row locks, theatre-then-surgeon order).
- **Automated by:** `SchedulingConcurrencyTest.concurrentSchedulingOfSameSlotNeverDoubleBooks`.
- **Status:** Automated — PASS (stable across repeated runs).

---

## Summary metrics

| Metric | Value |
|---|---|
| Total test cases | 34 |
| Automated — pass | 26 |
| Manual — pass | 2 (TC-SEC-06, TC-UX-01) |
| Deferred (documented reason + issue) | 6 (TC-RES-02/03/05, TC-SEC-01/03, TC-Rel-01) |
| Executed | 28 (82%) |
| Passed | 28 (100% of executed) |
| Failed | 0 |
| Blocked | 0 |

### Requirement coverage (RTM excerpt)

Every SRS requirement that is in scope for the current build maps to at
least one executed test case: OTMS-F-001/002 (auth + RBAC), F-004–F-009
(surgery + scheduling), F-010 (doctor availability), F-013/014 (conflicts),
F-015–F-018 (emergency workflow), F-019/020 (notifications), F-021 (theatre
availability maintenance), F-023 (utilisation reports), OTMS-NF-001
(performance), OTMS-NF-006 (concurrency), OTMS-SR-002/004/005 (RBAC,
unauthorized access, API validation). Deferred requirements (F-011, F-012,
F-022, SR-003, SR-001 deployment check, NF-002 monitoring) are tracked in
issues #13–#15 and the production checklist.

### Deferred items and their tracking

| Test case | Requirement | Reason | Tracking |
|---|---|---|---|
| TC-RES-02 | OTMS-F-011 Nurse availability | Nurse roster not in current data model | Issue #14 |
| TC-RES-03 | OTMS-F-012 Equipment availability | Equipment module not implemented | Issue #15 |
| TC-RES-05 | OTMS-F-022 Equipment maintenance | Equipment module not implemented | Issue #15 |
| TC-SEC-01 | OTMS-SR-001 TLS 1.2+ | TLS terminates at the production reverse proxy; local runs are plain HTTP | Production deployment checklist |
| TC-SEC-03 | OTMS-SR-003 Audit logging | Feature not yet implemented | Issue #13 |
| TC-Rel-01 | OTMS-NF-002 Availability | Needs deployed environment + uptime monitoring | Production deployment checklist |
