# OTMS Test Reports (Deliverables Part-3)

This folder holds the four required test reports. Generation is automated
where possible; each report maps to a Part-3 deliverable.

| Deliverable | Report | How to produce | Status |
|---|---|---|---|
| 2(b)i | Unit Test Report | `./scripts/generate-reports.sh` → `unit-backend/` (Surefire XML) + `unit-frontend/` (Vitest) | Pipeline ready — run on main after PRs #3/#4 merge |
| 2(b)ii | System Validation Report | Run `system-validation.postman_collection.json` against a live instance (see below), export results JSON here | Collection TODO (issue #21) |
| 2(b)iii | Code Coverage Report | Same script → `backend-jacoco/index.html` (line coverage) and `frontend-coverage/index.html` | Pipeline ready |
| 2(b)iv | Branch Coverage Report | Same script → JaCoCo report's **Branches** column per class | Pipeline ready |

## System validation run (TC-level pass through the REST API)

1. Start the backend: `cd backend && ./mvnw spring-boot:run` (Java 17).
2. In Postman: import `system-validation.postman_collection.json`, set
   collection variable `baseUrl` = `http://localhost:8080`, run the collection.
3. Export the run result (JSON) into a dated `docs/reports/<timestamp>/`
   folder and commit it on a branch via PR.

The collection exercises the SRS acceptance paths: TC-Auth-01/02 (login +
rejection), TC-SURG-01 (create), TC-SCH-02/03 (theatre overlap → 409),
TC-RES-01 (surgeon overlap → 409), TC-EMG-01 (emergency flag), TC-CON-01/02
(conflict detection + details in response), TC-REP-01 (utilization data).

## Notes

- CI also uploads coverage as workflow artifacts (backend-coverage-jacoco,
  frontend-coverage-v8) on every run — useful before reports are committed.
- JaCoCo branch coverage counts partially covered branches per method; a
  branch is fully covered only when both outcomes were executed by tests.
