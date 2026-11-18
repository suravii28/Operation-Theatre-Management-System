# Branch Coverage Report — conditional-path testing

**Evidence baseline:** GitHub `main` commit [`a983c69`](https://github.com/suravii28/Operation-Theatre-Management-System/commit/a983c69f9a3ac8c395f13503f8e4d3999652bc8e), [CI workflow run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), run on **18 November 2026 (UTC)**. These are CI results produced after the original **2–6 November** test window. They are *not* retroactively dated or claimed as that period's executions.

## Measured totals

| Suite | Branch metric | Observed coverage |
|---|---|---:|
| Backend JaCoCo | Covered 93 branches, missed 37 | **71.54%** (93/130) |
| Frontend Vitest/V8 | All-files branch coverage | **76.34%** |

Backend result derives from the root `BRANCH` counter in `jacoco.xml` inside artifact **14385678512**. Frontend result is the Vitest all-files coverage table in CI job **134489858554** (artifact **14385522919**). These are independent module measurements, not one combined system score.

## Backend package-level branch coverage

| Package | Covered / Total | Percentage |
|---|---:|---:|
| `com.otms.service` | 74/96 | 77.1% |
| `com.otms.controller` | 7/12 | 58.3% |
| `com.otms.config` | 2/8 | 25.0% |
| `com.otms.dto` | 9/12 | 75.0% |
| `com.otms.model` | 1/2 | 50.0% |

Other packages have no reported branch instructions; they are excluded from branch percentage breakdown.

## Frontend module-level branch coverage

| Module | Branch coverage |
|---|---:|
| API client | 41.66% |
| Authentication context | 100.00% |
| UI components | 63.63% |
| Pages | 88.04% |
| Utilities | 87.50% |

## Remaining branch coverage work

- Exercise malformed/expired JWT and missing-authentication paths in integration tests.
- Add validation and negative-role cases for theatre, staff, equipment, and surgery status transitions.
- Improve API-client request failure and invalid-response tests.
- Extend UI tests for interactive error paths and availability toggles.
- Maintain concurrent resource reservation stress tests for the nurse/equipment lock order.

## Reproduce

Backend: `cd backend && ./mvnw test` and inspect `target/site/jacoco/index.html`. Frontend: `cd frontend && npm ci && npm run test:coverage` and inspect `coverage/index.html`. See [CI run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242) for exact logged evidence.
