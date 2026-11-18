# Code Coverage Report — JaCoCo and Vitest V8

**Evidence baseline:** GitHub `main` commit [`a983c69`](https://github.com/suravii28/Operation-Theatre-Management-System/commit/a983c69f9a3ac8c395f13503f8e4d3999652bc8e), [CI workflow run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), run on **18 November 2026 (UTC)**. These are CI results produced after the original **2–6 November** test window. They are *not* retroactively dated or claimed as that period's executions.

The metrics below were extracted from **the coverage artifacts produced by this exact CI run**, not older coverage reports. [Workflow artifacts](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242) are named `backend-coverage-jacoco` (artifact **14385678512**) and `frontend-coverage-v8` (artifact **14385522919**).

## Coverage summary

| Metric | Backend (JaCoCo) | Frontend (Vitest/V8) |
|---|---:|---:|
| **Line coverage** | **84.93%** (575/677) | **79.64%** |
| Statement/instruction coverage | 83.48% instructions (2496/2990) | 79.26% statements |
| Function/method coverage | 74.88% methods (155/207) | 72.22% functions |
| Class coverage | 97.14% (34/35) | Not comparable to JaCoCo classes |

Different coverage tools use different denominators and instrumentation; do not add or average backend/frontend percentages. The backend percentage comes from `jacoco.xml` root `LINE` and `INSTRUCTION` counters, while frontend percentages are Vitest's `All files` total in its GitHub Actions log.

## Backend package line coverage

| Package | Covered / Total | Line coverage |
|---|---:|---:|
| `com.otms.service` | 285/304 | 93.8% |
| `com.otms.controller` | 79/94 | 84.0% |
| `com.otms.model` | 105/132 | 79.5% |
| `com.otms.config` | 91/129 | 70.5% |
| `com.otms.dto` | 14/15 | 93.3% |
| `com.otms` | 1/3 | 33.3% |

## Frontend module line coverage

| Module | Line coverage |
|---|---:|
| API client | 55.31% |
| Authentication context | 84.61% |
| Components | 91.30% |
| Pages | 84.35% |
| Utilities | 82.00% |

## Recommendation

Prioritize API client error/network handling, untested authentication/security branches, and complex report utility conditions. Export and retain the full workflow artifacts for viva examination. A percentage does not prove all high-risk behavior was tested; use test-case traceability and system validation alongside this report.
