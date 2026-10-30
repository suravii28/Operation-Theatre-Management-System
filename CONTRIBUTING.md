# Contributing to OTMS

This document is the developer guide for the OTMS team. It implements the team's ten
collaboration rules. For where each rule is technically enforced (CI, branch protection,
hooks), see [docs/git-workflow.md](docs/git-workflow.md).

## Rules at a glance

| # | Rule | Enforced by |
|---|------|-------------|
| 1 | One shared GitHub repository | `suravii28/Operation-Theatre-Management-System` |
| 2 | Every teammate is a collaborator | GitHub repository settings |
| 3 | CI runs tests automatically | `.github/workflows/ci-cd.yml` |
| 4 | `main` is protected; no direct unverified pushes | Branch protection on `main` |
| 5 | Each feature is developed in a separate branch | Branch naming + protected `main` |
| 6 | Every feature has an appropriate test case | `Test coverage guard` CI job + PR checklist |
| 7 | Tests run locally before pushing | `.githooks/pre-push` |
| 8 | PR with at least one teammate approval | Branch protection review rule |
| 9 | Merge only when CI is green and review is approved | Required check **CI Success** + review rule |
| 10 | Everyone pulls the latest `main` after merges | Team process (below) |

## One-time setup

```bash
git clone https://github.com/suravii28/Operation-Theatre-Management-System.git
cd Operation-Theatre-Management-System
./scripts/setup-dev.sh          # enables the pre-push test hook
```

Install dependencies once the code exists:

```bash
# Backend (Spring Boot)
cd backend && ./mvnw -q -DskipTests dependency:go-offline && cd ..

# Frontend (React)
cd frontend && npm ci && cd ..
```

Verify you are set up:

```bash
git config --get core.hooksPath      # should print .githooks
```

## Branch naming

Never commit directly to `main`. Create a branch from the latest `main`:

```bash
git switch main
git pull --ff-only
git switch -c feature/theatre-scheduling
```

| Prefix | Use for | Example |
|--------|---------|---------|
| `feature/` | New functionality | `feature/conflict-detection` |
| `fix/` | Bug fixes | `fix/booking-overlap` |
| `test/` | Test-only changes | `test/surgery-service-tests` |
| `docs/` | Documentation | `docs/api-reference` |
| `chore/` | Tooling, CI, config | `chore/ci-test-guard` |

Use short, lowercase, hyphenated descriptions.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add conflict detection for overlapping surgeries
fix: prevent double booking of theatre OT-2
test: add unit tests for SurgeryService
docs: update API section in README
chore: add test coverage guard to CI
```

One logical change per commit. Keep messages in the imperative mood.

## Tests are mandatory (rules 6 and 7)

Every feature or fix that changes application code must include a test.

- **Backend:** JUnit tests under `backend/src/test/java/...`
- **Frontend:** Vitest/Jest tests named `*.test.jsx|tsx` or `*.spec.jsx|tsx`

The `Test coverage guard` job fails a pull request when files under
`src/main/java` or frontend `src/` change without a matching test change.
Reference the test case IDs from the STP in the issue or PR.

Run tests locally before every push:

```bash
# Backend
cd backend && ./mvnw -B test

# Frontend
cd frontend && CI=true npm test
```

The `.githooks/pre-push` hook runs these automatically and blocks the push if they fail.
Do not bypass it with `SKIP_TESTS=1` unless you have a specific reason and tell the team.

## Pull requests (rules 8 and 9)

1. Push your branch:
   ```bash
   git push -u origin feature/theatre-scheduling
   ```
2. Open a pull request into `main` and fill in the template.
3. Link the issue (`Closes #N`) and request **at least one teammate** as reviewer.
4. Wait for the required check **CI Success** to turn green.
5. Address review comments, push fixes, and re-request review. A new push dismisses
   stale approvals, so the latest commit must be approved.
6. **Squash and merge** only after the review is approved and CI is green, then delete
   the branch.

Self-approval does not count. Administrators are subject to the same rules.

## Stay in sync (rule 10)

After any PR is merged, update your local `main` before starting the next branch:

```bash
git switch main
git pull --ff-only
git fetch --prune
git branch -d feature/theatre-scheduling   # if not auto-deleted
```

If your open branch is behind `main`, rebase it:

```bash
git fetch origin
git rebase origin/main
git push --force-with-lease
```

Branch protection requires your branch to be up to date with `main` before merging.

## Balanced, meaningful contributions

- One member does not merge everyone else's work: each member owns at least one
  feature issue per cycle.
- Rotate review duty so approvals are not always from the same person.
- Keep pull requests focused and reviewable; avoid dumping generated or unrelated files.
- Do not commit build output (`target/`, `node_modules/`, `dist/`), secrets, or `.env` files.
- Track ownership and progress on GitHub issues so contribution history stays balanced.

## Reporting problems

Open an issue (use the Feature template for features) and assign an owner. If CI or the
local hook blocks you, read the job log first; ask a teammate if it is still unclear.
