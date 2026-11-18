# Contributing to OTMS

## Roles and collaboration
See [CONTRIBUTORS.md](CONTRIBUTORS.md) for contributor roles and [story points](docs/story-points-2026-11.md) for retrospective estimates. Issue ownership does not imply someone else authored code: commits and PRs should reflect actual authors and reviewers.

## Local development
Prerequisites: Java 17, Node (version supported by `frontend/package.json` and Vite), npm and Git.

```bash
./scripts/setup-dev.sh
cd backend && ./mvnw spring-boot:run
# second terminal
cd frontend && npm ci && npm run dev
```

Local Vite at http://localhost:5173 proxies `/api` to Spring Boot port 8080. Seed credentials are documented in `backend/README.md` for **fictional local demo data only**.

## Feature and test workflow

1. Select an issue from the project backlog; confirm acceptance criteria and point estimate.
2. Start a feature branch from latest `main`: `git switch main && git pull --ff-only && git switch -c feature/<issue>-description`.
3. Implement the minimal change and update its related Maven/Vitest/Postman tests.
4. Run backend and frontend test suites before submitting a PR.
5. Open a PR linking the issue and include a summary and actual test evidence.
6. Ask a different teammate for review. An author may not present their own approval as independent review.
7. Merge only after relevant CI checks pass; **never force-push or rewrite shared `main` history**.

## Required commands

```bash
cd backend && ./mvnw test
cd ../frontend && npm ci && npm run lint && npm test && npm run test:coverage
# on a fresh running backend:
cd ..
npx newman run docs/reports/system-validation.postman_collection.json
node scripts/perf-check.js --requests 180 --concurrency 6
```

GitHub Actions workflows: `.github/workflows/ci-cd.yml` (backend, frontend, PostgreSQL, documentation and PR test guard) and `.github/workflows/api-system-validation.yml` (real HTTP system testing with Newman and performance).

## Documentation and safety

- Keep the SRS/SAD/STP baseline documents under version control and record deviations in `docs/sprint-plan-and-deviations.md`.
- Store current test reports in a dated subfolder of `docs/reports/`; attach reproducible test commands and current commit IDs.
- Preserve previous baseline results; do not backdate CI results or claim test cases were manually executed without evidence.
- Do not commit patient data, passwords, access tokens, secrets, or hospital identifiers.
- API documentation lives in `docs/api-reference-implemented.md`; architecture diagrams in `docs/architecture-implemented.md`.
- The submission checklist distinguishes work complete in GitHub from videos, real device tests, review approvals and manual project-board actions that remain to be done.
