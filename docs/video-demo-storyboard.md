# OTMS — walkthrough video plan (6–7 minutes)

**Aim:** demonstrate the **current `main` branch** with fictional seeded data. The script describes a planned recording, **not** an already recorded or executed test. Run the rehearsal first and replace any step that fails with an honest explanation.

## Setup / rehearsal

1. Pull current `main` into a **clean** working tree: `git switch main && git pull --ff-only origin main`.
2. Confirm Java 17 and a supported Node runtime; run `npm ci` in `frontend/` if dependencies aren't installed.
3. Terminal A: `cd backend && ./mvnw spring-boot:run` (H2 dev database, Flyway migrations, seeded fictional data; backend at `http://localhost:8080`).
4. Terminal B: `cd frontend && npm run dev` (Vite at `http://localhost:5173`, proxies `/api` to `:8080`).
5. Confirm login as `coordinator@otms.local` with `Coord123!` (development-only seed).
6. Use seeded records rather than freehand entry whenever possible. Nurse selection is required for new surgeries.
7. For API close-up, use Postman or curl with a bearer token obtained from login; do not expose the JWT or real passwords in the final recording.
8. Test each action before recording: API conflict `409`, emergencies impact, reschedule, notifications, and CSV/print.
9. Use one browser window, zoom to readable text, and mute desktop notifications.

## Screen-by-screen storyboard

| Segment | Time | Visual | Suggested narration |
|---|---|---|---|
| 1. Title and problem | 0:00–0:35 | Project title + architecture diagram | “Hospitals need coordinated theatre, surgeon, nurse, and equipment scheduling. OTMS centralizes operations and flags conflicting bookings.” |
| 2. Architecture | 0:35–1:10 | `docs/architecture-implemented.md` Mermaid diagram | “A React interface calls a Spring Boot REST backend. JWT guards requests; JPA and Flyway persist core records in H2 for the demo, with PostgreSQL supported through a production profile.” |
| 3. Login and dashboard | 1:10–1:55 | Login as coordinator, then Dashboard | “Staff sign in by role. The dashboard summarizes operations and theatre status.” |
| 4. Surgery scheduling and conflict | 1:55–3:15 | Surgeries page; show nurse, surgeon, theatre and time; attempt occupied slot | “The scheduler checks overlapping active operations and resource availability. A conflicting booking is rejected instead of double-booked.” **Rehearse this exact example first.** |
| 5. Emergency impact/reschedule | 3:15–4:25 | Emergencies page: choose seeded Lakshmi Rao emergency; click Impact; show Mohan Verma case; optionally move it to a verified free slot | “The system identifies routine surgeries affected by an emergency and assists the coordinator in rescheduling them.” Seeded scenario is **two days after backend startup**, 15:00–17:00, OT-1 / Dr. Priya. |
| 6. Theatre and notifications | 4:25–5:20 | Theatre availability (coordinator), Notifications and mark read | “Coordinators can change theatre availability; scheduling records event notifications for staff.” |
| 7. Reports + API & validation | 5:20–6:20 | Reports page CSV/print, quick curl/Postman sample, green CI run | “Management reports summarize utilization. REST APIs return structured errors, and automated CI checks the frontend, backend and database migrations.” |
| 8. Closing | 6:20–6:45 | Architecture slide / GitHub repo | “This prototype demonstrates conflict-aware theatre scheduling and emergency coordination. Further work includes audit logging, authentication hardening, and full end-to-end testing.” |

## Suggested on-screen REST API call

Use a local development account only:

```bash
curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"coordinator@otms.local","password":"Coord123!"}'
```

Then show a protected request via Postman, keeping the bearer token hidden. For a screenshot-worthy error, request a deliberately overlapping surgery with a **valid assigned nurse**, check the backend returns HTTP **409**, and display just the `message` from the response. Do not use a malformed `nurseId` request to claim conflict detection.

## Video QA checklist

- [ ] Current `main` builds, backend starts and login works.
- [ ] The actual seeded user/theatre/nurse IDs have been checked in the app.
- [ ] The planned conflicting example really returns 409, not an unrelated 400/404.
- [ ] Emergency impact returns a routine surgery; any reschedule shown uses an available slot.
- [ ] Notifications update, reports render, export/print work.
- [ ] Architecture diagram/API paths match **current `main`**, not an unmerged branch.
- [ ] No real patient data, visible JWT, secret keys or personal information.
- [ ] Narration does not claim audit logging, JWT refresh, per-user email notifications or live production deployment.

## After recording

Use a simple editor (QuickTime screen recording plus iMovie/CapCut or equivalent). Export MP4 (H.264) at 1080p with clear, readable UI and audible narration. Keep the recording concise and align the wording with the actual visible behavior. No video file has been generated by this document.
