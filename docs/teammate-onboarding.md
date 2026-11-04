# Teammate Onboarding Guide

Who this is for: anyone working on OTMS who has write access to the repo and needs
the exact commands, file paths, and click-paths. Nothing here is optional except the
"nice to have" notes.

---

## 0. What must be installed

| Tool | Version | Check command |
|---|---|---|
| Java (Temurin) | **17** — not 21/27 | `/usr/libexec/java_home -V` |
| Node.js | 20 or newer | `node -v` |
| Git | any recent | `git --version` |
| GitHub CLI (optional but handy) | 2.x | `gh --version` |

If Java 17 is missing, install it from https://adoptium.net (macOS aarch64 pkg) —
the build fails on Java 27.

---

## 1. First-time setup (run once)

Open **Terminal** (Cmd+Space → "Terminal") and run these one at a time. Each block
shows what you should see.

```bash
git clone https://github.com/suravii28/Operation-Theatre-Management-System.git
cd Operation-Theatre-Management-System
```

Install the local test hook (this is what enforces "run tests before pushing"):

```bash
./scripts/setup-dev.sh
```

Verify it took:

```bash
git config core.hooksPath
```

Expected output: `.githooks`

Make Java 17 permanent for your shell:

```bash
echo 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home' >> ~/.zshrc
source ~/.zshrc
java -version
```

Expected: `openjdk version "17.0.x"`. If it prints 27.x, your Temurin path differs —
run `/usr/libexec/java_home -v 17` and use that output in the export line.

Install frontend dependencies (one time only):

```bash
cd frontend && npm install && cd ..
```

Prove the whole toolchain works before you change anything:

```bash
cd backend && ./mvnw -B test && cd ..
cd frontend && CI=true npm test && cd ..
```

Expected: Maven ends with `BUILD SUCCESS` and 4 test classes; Vitest ends with
`Test Files 4 passed (4)` / `Tests 10 passed (10)`.

---

## 2. Running the app locally

You need **two terminal windows** (Cmd+N in Terminal for a second one).

Window 1 — backend (port 8080):

```bash
cd ~/Operation-Theatre-Management-System/backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./mvnw spring-boot:run
```

Wait for: `Started OtmsApplication in 4.xxx seconds`. Stop later with Ctrl+C.

Window 2 — frontend (port 5173):

```bash
cd ~/Operation-Theatre-Management-System/frontend
npm run dev
```

Wait for: `Local: http://localhost:5173/`. Then open that URL in a browser.

Sign in with any seeded account (H2 in-memory DB, reseeded on every restart):

| Email | Password | Role |
|---|---|---|
| `admin@otms.local` | `Admin123!` | ADMIN |
| `coordinator@otms.local` | `Coord123!` | COORDINATOR |
| `dr.ram@otms.local` | `Doctor123!` | DOCTOR |
| `dr.priya@otms.local` | `Doctor123!` | DOCTOR |
| `nurse@otms.local` | `Nurse123!` | NURSE |
| `tech@otms.local` | `Tech123!` | TECHNICIAN |

What you should see: Dashboard with 4 stat cards, a schedule table where Dr. Ram's
two overlapping cases carry a **Conflict** badge, and one emergency case
(Lakshmi Rao — Emergency laparotomy).

API-only smoke test (no browser needed):

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@otms.local","password":"Admin123!"}'
```

Expected: JSON with a `"token": "eyJ..."` field.

---

## 3. Your contributor commit (2 minutes, browser only)

1. Open: https://github.com/suravii28/Operation-Theatre-Management-System/new/main?filename=docs/team/yourname.md
2. Paste your ownership note (edit the issue numbers to yours).
3. Scroll to the bottom → select **"Create a new branch for this commit and start a pull request"**.
4. Branch name: `docs/yourname-intro` → **Propose changes** → **Create pull request**.
5. Ask Suravi to merge it. You now appear under Insights → Contributors.

---

## 4. How to approve a PR correctly

A comment is **not** an approval and does not satisfy branch protection.

1. Open the PR → **Files changed** tab.
2. Top-right green **Review changes** button.
3. Select **Approve** (or Request changes if something is wrong).
4. **Submit review**.

Check it registered: the PR header should show a green check with "1 review approved".

---

## 5. The issue loop (repeat for every issue)

```bash
git checkout main
git pull
git checkout -b feature/<issue-number>-<short-slug>
```

Write code **and** tests in the same branch — CI's test-guard job fails any PR that
changes `src/main/java/**` or `frontend/src/**` without a matching test change.

Run tests before pushing (the hook does this automatically, but run them yourself
first so you fix failures early):

```bash
cd backend && ./mvnw -B test && cd ..
cd frontend && CI=true npm test && cd ..
```

Commit, push, open the PR:

```bash
git add -A
git commit -m "feat: <what you did> (Closes #<issue-number>)"
git push -u origin feature/<issue-number>-<short-slug>
gh pr create --title "feat: <what you did>" \
  --body "Closes #<issue-number>

## What
- <bullet points>

## Tests
- <test class or file names>" \
  --reviewer sudhikshavaranasi
```

Fill the PR template prompts. `Closes #N` is what links and auto-closes the issue.

Watch CI:

```bash
gh pr checks <pr-number>
```

All five must pass: Docs validation, Backend tests, Frontend tests, Test coverage
guard, CI Success.

After it merges:

```bash
git checkout main && git pull
git branch -D feature/<issue-number>-<short-slug>
```

---

## 6. Per-issue file map

### #19 — Role-based endpoint guards

| Area | Files |
|---|---|
| Enable method security | `backend/src/main/java/com/otms/config/SecurityConfig.java` (add `@EnableMethodSecurity`) |
| Guard endpoints | `controller/SurgeryController.java`, `TheatreController.java`, `StaffController.java` (`@PreAuthorize("hasRole('ADMIN')")` etc.) |
| New test | `backend/src/test/java/com/otms/controller/SurgeryControllerRbacTest.java` |

Test pattern to copy — it already works in this project (see
`SurgeryControllerSecurityTest.java`): `@WebMvcTest` + `@Import({SecurityConfig.class, TestConfig.class})`
+ `@MockBean UserAccountRepository` + `@WithMockUser(roles = "NURSE")` and assert 403.

Run just your new test:

```bash
cd backend && ./mvnw -B test -Dtest=SurgeryControllerRbacTest
```

### #15 — Equipment records and availability

| Area | Files |
|---|---|
| New entity | `backend/src/main/java/com/otms/model/Equipment.java` (name, type, available flag) |
| New repository | `repository/EquipmentRepository.java` |
| Link to surgery | `model/Surgery.java` (add `@ManyToMany` or a `Set<Equipment>` join table) |
| Availability check | `service/SchedulingService.java` (reject when required equipment unavailable, throw `SchedulingConflictException`) |
| Status endpoint | `controller/EquipmentController.java` (`PATCH /api/equipment/{id}/status`) |
| DTOs | `dto/EquipmentRequest.java`, `dto/EquipmentResponse.java` |
| Seed data | `config/DataInitializer.java` (add 3-4 pieces of equipment) |
| Tests | `service/SchedulingServiceTest.java` (extend) + new `controller/EquipmentControllerTest.java` |

Schema note: `spring.jpa.hibernate.ddl-auto` is `update` in dev, so a new entity
creates its table automatically — no migration needed until #11.

### #20 — Full surgery record edit

| Area | Files |
|---|---|
| Backend | `controller/SurgeryController.java` (`PUT /api/surgeries/{id}`), `service/SchedulingService.java` (`update()` re-runs conflict checks excluding the record itself) |
| Frontend | `src/api/client.js` (`updateSurgery`), `src/pages/SurgeriesPage.jsx` (Edit button → reuse `SurgeryForm.jsx` prefilled) |
| Tests | backend: new `update` cases in `SchedulingServiceTest`; frontend: `src/test/SurgeryForm.test.jsx` extension |

The tricky bit: when re-checking conflicts you must ignore the surgery being edited,
otherwise it conflicts with itself.

### #8 — Reports export and print view

| Area | Files |
|---|---|
| New util | `frontend/src/util/csv.js` (rows → CSV string, escape quotes) |
| Page | `frontend/src/pages/ReportsPage.jsx` (Export CSV + Print buttons) |
| Styles | `frontend/src/styles.css` (`@media print` block) |
| Tests | `frontend/src/test/csv.test.js` (pure function) + a ReportsPage render test |

### #22 — NFR evidence (availability + usability)

Docs only: create `docs/nfr-evidence.md` with (a) deployment/HA notes for NF-002 and
(b) a 10-step usability walkthrough executed against the running UI for NF-005, each
step with observed result. No code, so no tests required.

### #11 — PostgreSQL + Flyway

| Area | Files |
|---|---|
| Dependency | `backend/pom.xml` (`flyway-core`, `flyway-database-postgresql`) |
| Migrations | `backend/src/main/resources/db/migration/V1__init.sql` (schema matching current entities) |
| Config | `backend/src/main/resources/application.yml` (`spring.flyway.enabled`, prod profile URL) |
| Local infra | `docker-compose.yml` at repo root (postgres 16, port 5432, `otms` db) |
| Docs | `backend/README.md` section "Running against PostgreSQL" |

Run it:

```bash
docker compose up -d
cd backend && ./mvnw -B spring-boot:run -Dspring-boot.run.profiles=prod
```

This one is the riskiest — if it slips past the deadline, move it to Sprint-3 and
note that in the demo.

---

## 7. When something fails

| Symptom | Cause | Fix |
|---|---|---|
| `Push blocked: local tests failed.` | Pre-push hook ran tests and one failed | Fix the test, or `SKIP_TESTS=1 git push` only for a docs-only change |
| Maven error about release version 17 / class file version | `JAVA_HOME` not set in that terminal | Re-run the `export JAVA_HOME=...` line in the same window |
| `Web server failed to start. Port 8080 was already in use.` | Old backend still running | `lsof -ti:8080 \| xargs kill` then start again |
| `npm error Cannot read properties of null (reading 'children')` | Version resolution clash | Install the exact matching major, e.g. `npm i -D @vitest/coverage-v8@^4.1.0` |
| CI **Test coverage guard** fails | Code changed without tests | Add the test file to the same branch and push again |
| Your approval disappeared | New commits were pushed to the PR (stale-review dismissal) | Re-approve after the final push |
| PR says "Merge blocked" / branch out of date | Another PR merged first | `git fetch && git rebase origin/main && git push --force-with-lease` |

---

## 8. Rules that never change

1. Never push to `main` directly — it is protected and will reject you.
2. One issue → one branch → one PR.
3. Code and tests in the same PR.
4. One teammate approval required; never approve your own PR.
5. Merge only after all CI checks are green.
6. Pull `main` before starting new work.
