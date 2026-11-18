# Part-2 GitHub Project board verification — evidence record

**Prepared:** 18 November 2026 | **Repository:** `suravii28/Operation-Theatre-Management-System`

## Findings that are independently verified

- Repository `CONTRIBUTORS.md` provides a [GitHub Project link](https://github.com/users/suravii28/projects/1) and enumerates four project roles.
- GitHub issues carry specific issue assignees; backlog includes SRS functional and non-functional requirements (for example #8 reports, #9 API tests, #12 security, #13 audit, #14 nurse scheduling, #16 emergency impact, #21 performance, #22 reliability/usability).
- Story-point estimates are written in the issue bodies as retrospective annotations and summarized in [story-point allocation](story-points-2026-11.md).
- Sprint themes and the retrospective mapping are in [sprint plan and deviations](sprint-plan-and-deviations.md).
- The [PRs and GitHub Actions checks](https://github.com/suravii28/Operation-Theatre-Management-System/pulls) document review, CI, and merge activity.

## Part-2 board acceptance matrix

| Part-2 requirement | Repository verification | Native GitHub Project field verification |
|---|---|---|
| GitHub Project created and linked | URL appears in `CONTRIBUTORS.md`; linked URL is `https://github.com/users/suravii28/projects/1` | **NEEDS MANUAL OPEN** — exact board existence, repository link and access permissions not confirmed |
| FR/NFR backlog populated | SRS-mapped repository issues exist | **NEEDS MANUAL CHECK** — confirm every issue is also a Project item |
| Story points on all backlog items | Points annotated on repository issues #5–#23 and #30–#31; retrospective summary in `docs/story-points-2026-11.md` | **NEEDS MANUAL CHECK** — Project custom field “Story Points” may be missing or empty |
| Each story assigned to a teammate | Repository issues have assignees; see `CONTRIBUTORS.md` | **NEEDS MANUAL CHECK** — Project Assignee field may not mirror GitHub issues |
| Sprint 1 and Sprint 2 planned | Sprint mapping is documented in the repository | **NEEDS MANUAL CHECK** — Project iteration/sprint fields not accessible through the available GitHub integration |

**Why these remain unverified:** the connected GitHub repository API exposes issues, assignees, commits, PRs, and Actions, but it does **not** expose the user-owned GitHub Projects v2 board's custom fields and iteration memberships in this session. A documented URL is not proof that all fields have been filled.

## Exact final check on GitHub

Open **[OTMS Project Board](https://github.com/users/suravii28/projects/1)** using the owner account. In the board's **Table** view:

1. Verify the project title and repository association under Settings / Linked repositories (if the Project offers this).
2. Verify every FR/NFR issue is included in the board. An issue existing in the repository alone doesn't count as a Project item.
3. Create/show a numeric **Story Points** field and enter point estimates for every non-duplicate backlog item, using the corresponding issue descriptions as the reference. If the board already has a points field, reuse it; do not duplicate.
4. Use an Iteration field or another explicit `Sprint` field to place the historical Sprint 1 and Sprint 2 items, and the optional testing/spillover tasks in Sprint 3. Use documented scope, not fabricated original dates.
5. Verify all items have an appropriate assignee and status (Todo/In progress/Done) matching actual merged work and open issues.
6. Capture **one screenshot of Table view** showing Issue, Assignees, Story Points, Sprint, and Status columns, and **one screenshot of iteration/board grouping** for the submission.
7. Record who inspected it and when below, after opening the board.

## Verification sign-off (not yet performed)

| Field | Actual entry |
|---|---|
| Board verified by | _Pending_ |
| Verification date | _Pending_ |
| GitHub Project access / repository link | _Pending confirmation_ |
| Story Points populated natively | _Pending confirmation_ |
| Sprint 1 / Sprint 2 grouped natively | _Pending confirmation_ |
| Proof screenshots saved | _Pending_ |

**Do not check off the native Project board requirement until this section is completed using the live board.**
