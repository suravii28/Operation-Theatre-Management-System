# OTMS development freeze and final submission gates

**Prepared:** 18 November 2026

## Freeze scope and decision

**Development feature-freeze candidate:** `main` SHA `288a8b144d5f5e6d594395de38ebae696beb8e4c` (merge of approved PR #58). This is the proposed **code baseline** for Part-2/Part-3 deliverables. The team should avoid merging new optional features into this baseline while the final demo/test evidence is being evaluated.

**Status:** **FEATURE FREEZE PROPOSED — pending team acceptance; UAT and board verification not yet signed.** A passing CI run does not by itself constitute final release acceptance.

**Excluded from frozen baseline:** unmerged PR [#53](https://github.com/suravii28/Operation-Theatre-Management-System/pull/53), theatre date selector/notification filters. It has passed branch CI, but it has no approved review yet. Do not include its features in the frozen `main` demo unless explicitly merged and retested.

**Out of scope until later sprint or documented exception:** login rate limiting, token refresh, account lockout (issue #12); dedicated audit-log entity/API (issue #13); production availability monitoring (issue #22); complete nurse shift/leave roster; full role-by-role browser E2E, beyond the completed smoke suite. These are actual limitations, not already completed features.

## Verified automatic gates

| Gate | Evidence | Status |
|---|---|---|
| Maven and frontend component tests, lint, docs | [Main CI run #44211242242](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44211242242) and updated approved PR #58 CI | **PASS** |
| H2 and PostgreSQL/Flyway migrations | Main CI/PostgreSQL integration job | **PASS** |
| Full API integration on local test server | [Newman run #44213680844](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44213680844): 33 HTTP requests, 36 assertions | **PASS** |
| Normal load response-time budget | Same run: 180 requests, 0 failed, p90 32.7 ms vs 3000 ms | **PASS** |
| Browser smoke | [Chromium run #44215079683](https://github.com/suravii28/Operation-Theatre-Management-System/actions/runs/44215079683): 4 journeys | **PASS** |
| PR #58 reviewed before merge | PR #58 approved by another reviewer, merged into main | **PASS** |
| Current frozen SHA's own CI | Check newest main Actions run and fill exact result here before declaring final release | **PENDING final review** |

## Outstanding acceptance gates

- [ ] GitHub Project v2 board manually verified (native points and sprint/iteration fields); save evidence in `docs/part2-board-verification.md`.
- [ ] Complete 15 manual UAT cases in `docs/manual-uat-signoff.md`, attach dated screenshots, collect QA/product owner approval.
- [ ] Review open SRS/NFR issues and record conscious deferrals; issue status must not be falsified for submission.
- [ ] Decide on PR #53: explicitly defer or merge with review and rerun all tests, which requires updating the frozen SHA.
- [ ] Record the exact final main SHA and results from a CI run against that SHA.
- [ ] Owner and QA lead approve the final baseline.
- [ ] Upload the two videos: Sprint 1 terminal-only (1 minute), Sprint 2 React UI/results (2 minutes).

## Changes allowed after feature freeze

Allowed: verified bug fixes, test corrections, deliverable documentation, evidence, team review comments and recordings, each on normal PRs with CI and review.

Not allowed without team re-approval: merging unreviewed optional features, rewriting/backdating Git author history, reporting unexecuted tests as PASS, or editing production secrets into the repo.

## Team sign-off block

| Role | Name | Actual approval/date |
|---|---|---|
| Development lead / repository owner | Suravi | _Pending_ |
| Product owner | Shesanth | _Pending_ |
| QA lead | Sudhiksha | _Pending_ |
| Operations / domain representative | Praneetha | _Pending_ |

**Formal release-freeze decision:** _Not yet signed_. This document identifies a stable candidate commit and a reproducible closure process; it does **not** impersonate team approval.
