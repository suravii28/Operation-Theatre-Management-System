# Browser End-to-End Smoke Report — Chromium

**Executed:** 18 November 2026 UTC · **Branch:** `submission/part3-testing-reports-readiness`, workflow commit `4440a52342d4fdd4bbd370ac54ffed96064e17d2`.

**Verified GitHub Actions run:** [Browser E2E smoke #44215079683](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44215079683). **Outcome: PASS**, 4 automated browser checks, 0 failures. The action spun up the real Spring Boot backend, seeded H2 database, React/Vite dev server and Chromium using Playwright, and uploaded artifact **14387399304** containing its machine-readable result and runtime logs.

| Browser journey | Observed result |
|---|---|
| Login with development COORDINATOR and load Dashboard | **PASS** |
| Navigate to Surgeries through React Router | **PASS** |
| Open Emergencies, display impact analysis and verify displaced seeded routine surgery | **PASS** |
| Open Reports and download an actual CSV report file | **PASS** |

**Test implementation:** `scripts/browser-smoke.cjs` · **CI:** `.github/workflows/browser-e2e.yml`.

## Scope and limitations

This is a minimal **4-check smoke test**, not full Playwright/Cypress coverage of every SRS scenario. Authentication failures, mutation paths in the UI, every authorized role, emergency rescheduling confirmation, booking conflict/409 error in the browser and manual accessibility/usability checks remain to be expanded. Backend concurrency and scheduling validation are exercised separately by Maven/HTTP Newman suites. It cannot establish deployed reliability, performance under hospital-scale concurrency, or compliance with security/audit requirements.

The browser test was executed **18 November**, not backdated to the original **2–6 November** testing window. Its screenshot/logs are held in GitHub Actions; the GitHub artifact may contain development-only credential details in backend logs, so share the summarized report for public review.

## Reproduction

Start seeded backend on port 8080, frontend on port 5173, and install Playwright with Chromium. Set `E2E_EMAIL`, `E2E_PASSWORD` and optionally `PLAYWRIGHT_MODULE_PATH`, then run `node scripts/browser-smoke.cjs`. CI handles these prerequisites automatically.

## Relationship to Part-3 deliverables

This report supplements, rather than replaces, the four required artifacts:
[Unit](unit-test-report.md) · [System Validation](system-validation-report.md) · [Code Coverage](code-coverage-report.md) · [Branch Coverage](branch-coverage-report.md).
