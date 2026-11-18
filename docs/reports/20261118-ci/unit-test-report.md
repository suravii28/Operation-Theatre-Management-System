# Unit Test Report — current CI baseline

**Evidence baseline:** GitHub `main` commit [`a983c69`](https://github.com/suravii28/Operation-Theatre-Management-System/commit/a983c69f9a3ac8c395f13503f8e4d3999652bc8e), [CI workflow run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), run on **18 November 2026 (UTC)**. These are CI results produced after the original **2–6 November** test window. They are *not* retroactively dated or claimed as that period's executions.

## Method and observed outcome

| Suite | Command run in GitHub Actions | Tests | Passed | Failed | Errors | Skipped |
|---|---|---:|---:|---:|---:|---:|
| Backend Spring Boot / Maven Surefire | `./mvnw -B test` | 65 | 65 | 0 | 0 | 0 |
| Frontend React / Vitest | `npm test --if-present` | 31 | 31 | 0 | 0 | 0 |
| Frontend test coverage re-run | `npm run test:coverage --if-present` | 31 | 31 | 0 | 0 | 0 |
| Backend tests against PostgreSQL | `./mvnw -B test` with `SPRING_PROFILES_ACTIVE=prod` | Suite completed successfully | — | 0 reported | — | — |

**Total unique test cases demonstrated in the normal suites:** **96** (65 backend + 31 frontend). The 31 coverage-mode frontend tests are a *repeat*, not 31 additional distinct tests. The PostgreSQL run repeats backend tests with a different persistence target, rather than adding 65 new unique cases.

## Backend test classes (from workflow log)

| Test class | Passed |
|---|---:|
| `FlywayMigrationTest` | 1 |
| `OtmsApplicationTests` | 1 |
| `SchedulingConcurrencyTest` | 1 |
| `SchedulingServiceTest` | 27 |
| `TheatreControllerTest` | 8 |
| `NotificationControllerTest` | 4 |
| `EquipmentControllerTest` | 7 |
| `SurgeryControllerSecurityTest` | 11 |
| `AuthControllerTest` | 3 |
| `DataInitializerTest` | 2 |
| **Total** | **65** |

Frontend: **9 test files, 31 tests**, all passed (Vitest log). GitHub Actions frontend lint and docs-validation jobs also passed. Flyway successfully validated and applied **three** migrations to development H2 and to PostgreSQL in the separate integration job.

## Evidence and reproducibility

- [Backend tests job](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), job **134489858829**.
- [Frontend tests job](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), job **134489858554**.
- [PostgreSQL integration job](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), job **134489858999**.
- Commands to reproduce locally: `cd backend && ./mvnw test`; `cd frontend && npm ci && npm test && npm run test:coverage`.

## Limitations

This report is based on **actual GitHub CI logs**, not a local run performed by the report author. It does not prove manual usability tests, live API/Postman tests, distributed-load endurance, or production deployment. The historical reports in `docs/reports/20261031-sprint2/` remain separate baseline evidence.
