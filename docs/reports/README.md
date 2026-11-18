# OTMS Part-3 Testing and QA Reports

**Latest verified baseline:** `main` commit `a983c69f9a3ac8c395f13503f8e4d3999652bc8e`, [CI run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242), **18 November 2026**. These tests occurred **after** the assignment's specified **2–6 November** testing window, not during it.

## Required reports

| Part-3 deliverable | Current report | Measured evidence |
|---|---|---|
| (i) Unit Test Report | [unit-test-report.md](20261118-ci/unit-test-report.md) | Backend 65/65 pass; frontend 31/31 pass |
| (ii) System Validation Report | [system-validation-report.md](20261118-ci/system-validation-report.md) | CI controller/integration passes; live Newman suite and performance evidence tracked with separate workflow |
| (iii) Code Coverage Report | [code-coverage-report.md](20261118-ci/code-coverage-report.md) | JaCoCo backend **84.93% lines**; V8 frontend **79.64% lines** |
| (iv) Branch Coverage Report | [branch-coverage-report.md](20261118-ci/branch-coverage-report.md) | JaCoCo backend **71.54% branches**; V8 frontend **76.34% branches** |

Full coverage assets are available from the **actual run**, in GitHub Actions artifacts `backend-coverage-jacoco` (ID 14385678512) and `frontend-coverage-v8` (ID 14385522919). These reports should be read alongside the updated [Test Case Specification v3](../test-cases.md) and the [submission checklist](../submission-readiness.md).

## Historical evidence

`20261031-sprint2/` is the archived Sprint-2 baseline; preserve it rather than relabel it as the current run. Existing reports generated from older commits do not automatically validate subsequent changes, including nurse assignment and the theatre API status endpoint.

## Running tests

```bash
# Java 17 backend unit/service/integration tests
cd backend
./mvnw test

# React/Vitest and coverage
cd ../frontend
npm ci
npm test
npm run test:coverage

# Live REST API system tests: run on a freshly started backend
cd ..
npx newman run docs/reports/system-validation.postman_collection.json \
  --reporters cli,json \
  --reporter-json-export docs/reports/20261118-ci/system-validation-newman-results.json

# Normal-load HTTP performance validation
node scripts/perf-check.js --requests 180 --concurrency 6 \
  --out docs/reports/20261118-ci/performance-results.json
```

GitHub workflow `.github/workflows/api-system-validation.yml` runs the full live API/Postman suite, captures Newman output, runs the performance probe and uploads JSON/log artifacts. It requires GitHub Actions to finish successfully before claiming its results as verified.

## Interpreting acceptance

CI unit/component coverage is a prerequisite but **not** complete acceptance testing. Manual UI usability, uptime, dedicated audit logging, security hardening and an end-to-end browser flow still need explicit results or documented deviations. Don't mark these passed without actually executing them.
