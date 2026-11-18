# OTMS submission readiness checklist (Part-2 / Part-3 / Part-4)

**Reviewed:** 18 November 2026. This checklist distinguishes *repository evidence* from acceptance or actions that require actual humans. A checked-looking CI status is not equivalent to a completed manual task. The formal original testing window was **2–6 November 2026**; the latest CI evidence below was generated **18 November**.

## Part-2: Agile planning and repository setup

| Rubric item | State | Evidence / action |
|---|---|---|
| GitHub Project created and linked to repository | **NEEDS BOARD CHECK** | [Project board link](https://github.com/users/suravii28/projects/1) is documented in `CONTRIBUTORS.md`, but board membership/fields could not be independently verified with the available repository API |
| FR/NFR backlog items | **PRESENT** | Issues #5–#23 and additional QA/documentation issues #30–#31 map to SRS requirements |
| Story points assigned | **DOCUMENTED IN REPO** | `docs/story-points-2026-11.md` provides estimates; verify native Project Story Points custom field separately |
| Individual assignment | **PRESENT** | GitHub issue assignees and `CONTRIBUTORS.md` identify owners; many merged PR authors may differ from issue owner |
| Sprint-1 and Sprint-2 board plans | **NEEDS BOARD CHECK** | Proposed retrospective grouping described in `docs/sprint-plan-and-deviations.md`; board iteration fields cannot be verified |
| GitHub Actions used | **VERIFIED** | [CI run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242) passed on main; PR workflows configured |
| Code implemented against SRS/SAD | **SUBSTANTIALLY IMPLEMENTED** | React, Spring Boot, JWT, scheduling, nurse/equipment checks, emergency workflow, reports; see `docs/remaining-work-audit.md` for incomplete cases |
| PR raised by assigned teammate | **MIXED / REVIEW REQUIRED** | GitHub evidence includes PRs opened under the repo owner account for issues owned by other teammates; cannot certify every issue's PR authorship |
| PR formally reviewed by owner | **NEEDS REVIEW RECORDS** | GitHub merge status alone does not prove approval; inspect review history per PR |
| Sprint-1 1-minute terminal video | **PENDING USER RECORDING** | Storyboard: `docs/video-demo-storyboard.md`; deliver later |

## Part-2: Sprint-2 execution and finalization

| Rubric item | State | Evidence / action |
|---|---|---|
| Sprint-2 development completed | **CORE FEATURES PRESENT** | Backend, frontend, conflict detection, emergency impact, notifications, reports and migrations on main |
| Official development freeze | **NOT YET DECLARED** | Open draft PR #53 and current testing PR #58 mean further changes are in flight; freeze explicitly after green CI and merge decision |
| Deviations from SAD/SRS documented | **DOCUMENTED** | `docs/sprint-plan-and-deviations.md` and `docs/remaining-work-audit.md` |
| Final project documentation | **IN PROGRESS** | SRS/SAD/STP PDF baseline exists; updated API, architecture, testing and submission docs in GitHub |
| Sprint-2 2-minute UI/results video | **PENDING USER RECORDING** | Capture app and current results later |

## Part-3: Testing and QA

| Rubric item | State | Evidence / action |
|---|---|---|
| Test cases updated | **UPDATED IN PR #58** | `docs/test-cases.md` v3 and revised Postman collection |
| Testing per STP completed | **PARTIAL** | Backend, frontend and PostgreSQL CI suites pass; new live Newman/system check and manual items need execution |
| Unit Test Report | **GENERATED** | `docs/reports/20261118-ci/unit-test-report.md` — 65 backend + 31 frontend tests passed |
| System Validation Report | **GENERATED, EXECUTION PARTIAL** | `docs/reports/20261118-ci/system-validation-report.md` — distinguishes observed CI evidence from pending live HTTP cases |
| Code Coverage Report | **GENERATED** | `docs/reports/20261118-ci/code-coverage-report.md` — measured JaCoCo/V8 artifacts |
| Branch Coverage Report | **GENERATED** | `docs/reports/20261118-ci/branch-coverage-report.md` — measured JaCoCo/V8 artifacts |
| Reports uploaded to `docs/` | **IN PR #58** | These files are committed on the submission feature branch; merge after review and CI |
| Optional Sprint-3 | **APPLICABLE** | Nurse/theatre availability and integrations landed after earlier sprint baselines; describe as spillover/integration, not fictitious historical work |

## Part-4: Final evaluation / viva

| Rubric item | State | Evidence / action |
|---|---|---|
| Working demo of core use cases | **PENDING LIVE WALKTHROUGH** | Main CI passes; run actual login, booking, rejected conflict, emergency impact, notifications and CSV export before recording |
| Permitted design deviations explained | **DOCUMENTED** | Architecture and deviations docs |
| Readable/versioned code | **PRESENT** | Public repository and PR history; latest main CI green |
| Individual code contribution explanations | **TEAM ACTION** | Each member must review actual source files, their accepted PRs and assigned issues before viva |
| Agile process explanation | **DOCUMENTED** | Project roles, point estimates, issue owners, PR workflow, sprint scope and outstanding backlog |
| API/architecture/testing justification | **DOCUMENTED** | `docs/architecture-implemented.md`, `docs/api-reference-implemented.md`, four CI test reports |

## Blocking items before claiming full submission completeness

1. Run and inspect the current API Newman workflow (and save its actual output), not just the test collection.
2. Perform actual manual usability walkthrough and system-level demo; attach results to `docs/`.
3. Confirm GitHub Project sprint iterations/point values in the board.
4. Obtain teammate reviews and explicit freeze confirmation; record final main SHA.
5. Record/upload two videos (one-minute terminal Sprint 1; two-minute UI Sprint 2).
6. Resolve or explicitly sign off on open security/audit/E2E/reliability scope (#9, #10, #12, #13, #21, #22, #30, #31).
