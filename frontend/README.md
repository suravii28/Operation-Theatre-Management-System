# OTMS Frontend

React UI for the Operation Theatre Management System.

## Stack

- React 19 + Vite
- React Router (protected routes, role shown from JWT payload)
- Vitest + Testing Library for tests

## Run locally

Requires the backend running on http://localhost:8080 (see `backend/README.md`).

```bash
npm ci
npm run dev
```

Open http://localhost:5173. The dev server proxies `/api/*` to the backend.

Seeded logins (see the backend README): `admin@otms.local` / `Admin123!`, or
`coordinator@otms.local` / `Coord123!`.

## Pages

| Route | Purpose |
|-------|---------|
| `/login` | JWT sign-in |
| `/` | Dashboard: today's stats, conflicts, emergencies |
| `/surgeries` | Browse/filter surgeries, schedule new ones (409 conflicts surfaced) |
| `/theatres` | Per-theatre occupancy for today |
| `/emergencies` | Emergency queue with status transitions |
| `/reports` | Utilisation summary per theatre and per status |

## Tests

```bash
npm test
```

## Build

```bash
npm run build
```
