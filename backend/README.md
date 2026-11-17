# OTMS Backend

Spring Boot 3 (Java 17) REST API for the Operation Theatre Management System.

## Stack

- Spring Web, Spring Data JPA, Spring Security (JWT)
- H2 in-memory database for local dev and CI
- PostgreSQL via the `prod` profile

## Run locally

```bash
./mvnw spring-boot:run
```

The API starts on http://localhost:8080. Demo data is seeded automatically
(users, theatres, and surgeries, including one deliberate scheduling conflict).

Seeded login (local dev only):

| Email | Password | Role |
|-------|----------|------|
| admin@otms.local | Admin123! | ADMIN |
| coordinator@otms.local | Coord123! | COORDINATOR |
| dr.ram@otms.local | Doctor123! | DOCTOR |

Log in to get a JWT:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@otms.local","password":"Admin123!"}'
```

Then call a protected endpoint:

```bash
curl -s http://localhost:8080/api/surgeries \
  -H "Authorization: Bearer <token>"
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/theatres` | List active theatres |
| GET | `/api/staff?role=DOCTOR` | List staff (optionally by role) |
| GET | `/api/surgeries?date=YYYY-MM-DD&theatreId=&status=` | List surgeries with filters |
| GET | `/api/surgeries/emergencies` | List active emergency surgeries |
| GET | `/api/surgeries/{id}` | Get one surgery |
| POST | `/api/surgeries` | Schedule a surgery (409 on conflict) |
| PATCH | `/api/surgeries/{id}/status` | Update surgery status |

Scheduling a surgery that overlaps an active surgery in the same theatre, or
with the same lead surgeon, is rejected with `409 Conflict`.

## Tests

```bash
./mvnw test
```

## Production profile

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`, then run with
`--spring.profiles.active=prod`. Seeding is disabled in `prod`.
