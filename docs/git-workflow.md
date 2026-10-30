# OTMS Git and GitHub Workflow

This document explains how the team's ten collaboration requirements are implemented, and
what an administrator must configure on GitHub. Developer instructions live in
[CONTRIBUTING.md](../CONTRIBUTING.md).

## Requirement-to-enforcement map

| # | Requirement | Implementation |
|---|-------------|----------------|
| 1 | One common repository | `https://github.com/suravii28/Operation-Theatre-Management-System` |
| 2 | All teammates as collaborators | GitHub → Settings → Collaborators: `PraneethaSreedharala`, `shestg28`, `sudikshavaranasi` (plus owner `suravii28`) |
| 3 | CI runs tests automatically | `.github/workflows/ci-cd.yml` runs on every pull request to `main` and on pushes to `main`: docs validation, Maven backend tests, Node frontend tests |
| 4 | Protect `main`, no direct unverified pushes | Branch protection: pull request required, required status check `CI Success`, force pushes and deletions blocked, rules apply to admins |
| 5 | Each feature in its own branch | Protected `main` blocks direct pushes; `CONTRIBUTING.md` defines `feature/`, `fix/`, `test/`, `docs/`, `chore/` prefixes |
| 6 | Every feature has a test | `Test coverage guard` job fails a PR when `src/main/java` or frontend `src/` changes without a test change; PR template and issue template require test evidence |
| 7 | Run tests locally before pushing | `.githooks/pre-push` runs backend and frontend tests; enabled per clone with `./scripts/setup-dev.sh` |
| 8 | PR with at least one teammate approval | Branch protection requires 1 approving review and dismisses stale approvals on new pushes |
| 9 | Merge only after CI passes and review is approved | Required check `CI Success` plus required review; branch must be up to date with `main` |
| 10 | Pull latest `main` after merging | Team process in `CONTRIBUTING.md`; "branches must be up to date" forces a fresh rebase/pull before merge |

## CI jobs

Workflow: `.github/workflows/ci-cd.yml` (name `CI`).

| Job | Required | What it does |
|-----|----------|--------------|
| `Docs validation` | yes | Confirms `SAD_OTMS.pdf`, `SRS_OTMS.pdf`, `STP_OTMS.pdf` exist and are valid PDFs |
| `Backend tests (Maven)` | yes | Runs `./mvnw -B test` (or `mvn -B test`) when `backend/pom.xml` or root `pom.xml` exists; skips with a notice until the backend is added |
| `Frontend tests (Node)` | yes | Runs `npm ci`/`npm install`, `npm run lint`, `npm test` when `frontend/package.json` or root `package.json` exists; skips with a notice until the frontend is added |
| `Test coverage guard` | yes (PRs) | Fails when application code changes without tests |
| `CI Success` | **the only required check** | Aggregates the jobs above; this is the check name to require in branch protection |

Keeping a single required check (`CI Success`) means branch protection does not break when
the backend or frontend is added later, and skipped jobs do not block merges.

## Administrator setup

Only a repository administrator needs to do this, once.

### 1. Confirm collaborators (rule 2)

Settings → Collaborators and teams → verify all teammates have at least **Write** access.

### 2. Enable Actions (rule 3)

The **Actions** tab must be enabled. The workflow runs automatically on pull requests.

### 3. Protect `main` (rules 4, 8, 9)

**Option A — script (fastest, needs GitHub CLI):**

```bash
brew install gh
gh auth login
./scripts/setup-branch-protection.sh
```

**Option B — GitHub UI:**

Settings → Branches → **Add branch protection rule** for `main`:

- [x] Require a pull request before merging
  - Required approvals: **1**
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require approval of the most recent reviewable push
- [x] Require status checks to pass before merging
  - Required check: **CI Success**
  - [x] Require branches to be up to date before merging
- [x] Require conversation resolution before merging
- [x] Do not allow force pushes
- [x] Do not allow deletions
- [x] Include administrators

The `CI Success` check appears in the list after the workflow has run at least once.

## Developer workflow (summary)

```bash
# 1. Start from fresh main
git switch main && git pull --ff-only

# 2. Branch per feature (rule 5)
git switch -c feature/conflict-detection

# 3. Work, add tests (rule 6), run tests locally (rule 7)
git add <files>
git commit -m "feat: add conflict detection"

# 4. Push (pre-push hook runs tests) and open a PR (rule 8)
git push -u origin feature/conflict-detection
gh pr create --fill --reviewer sudikshavaranasi

# 5. Wait for CI Success + approval, then squash and merge (rule 9)

# 6. Sync main (rule 10)
git switch main && git pull --ff-only && git fetch --prune
```

## Verifying the setup

1. Push a commit to a feature branch and open a PR: `Docs validation`, backend/frontend
   jobs, `Test coverage guard`, and `CI Success` appear.
2. Change a backend `src/main/java` file without a test: the guard fails.
3. Try `git push origin main`: GitHub rejects the push (rule 4).
4. Open a PR with a red check: the merge button is disabled (rule 9).
5. Open a PR without approval: merging is blocked until a teammate approves (rule 8).
