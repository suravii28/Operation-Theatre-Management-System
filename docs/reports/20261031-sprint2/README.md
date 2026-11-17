# OTMS Test Reports — Sprint-2 run (2026-10-31, frontend added 2026-11-04)

Generated from `main` with `./scripts/generate-reports.sh` (backend) and
`npm run test:coverage` (frontend). System validation was executed with Newman
against a live backend.

## Summary

| Report | Deliverable | Result |
|---|---|---|
| Unit Test Report (backend) | Part-3 2(b)i | 4 test classes, all passing — `unit-backend/surefire-reports/` |
| Unit Test Report (frontend) | Part-3 2(b)i | 4 test files, **10 tests passed** — `unit-frontend/vitest-summary.txt` |
| System Validation Report | Part-3 2(b)ii | 9 requests / 9 assertions, **0 failures** — `system-validation/results.json` |
| Code Coverage Report (backend) | Part-3 2(b)iii | **70.9%** line coverage — `backend-jacoco/index.html` |
| Branch Coverage Report (backend) | Part-3 2(b)iv | **42.9%** — JaCoCo "Branches" column, `backend-jacoco/jacoco.csv` |
| Code Coverage Report (frontend) | Part-3 2(b)iii | **81.9%** lines / **80%** statements — `frontend-coverage/index.html` |
| Branch Coverage Report (frontend) | Part-3 2(b)iv | **64.15%** branches — same report, "Branches" column |

## Backend classes under test

- `com.otms.service.SchedulingServiceTest` — conflict rules (theatre overlap, surgeon
  double-booking, emergency flagging, status transitions)
- `com.otms.controller.AuthControllerTest` — login success/failure
- `com.otms.controller.SurgeryControllerSecurityTest` — 401/403/400/409 responses
- `com.otms.OtmsApplicationTests` — context load

## System validation coverage

| Request | SRS test case | Result |
|---|---|---|
| Login, valid credentials | TC-Auth-01 | pass |
| Login, invalid credentials → 401 | TC-Auth-02 | pass |
| Protected endpoint without token | TC-Sec-05 | pass |
| Surgery list with token | TC-SURG-03 | pass |
| Create valid surgery | TC-SURG-01 | pass |
| Overlapping theatre slot → 409 + details | TC-SCH-02 / TC-SCH-03 / TC-CON-02 | pass |
| Surgeon double-booking → 409 | TC-RES-01 | pass |
| Emergency list | TC-EMG-01 | pass |
| Status transition → IN_PROGRESS | TC-SURG-02 | pass |

## Reproduce

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
cd backend && ./mvnw -B clean test && cd ..
./mvnw -q spring-boot:run &   # in backend/
npx newman run docs/reports/system-validation.postman_collection.json \
  --reporters cli,json --reporter-json-export docs/reports/<run>/system-validation/results.json
```
