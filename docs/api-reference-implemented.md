# OTMS — REST API reference (as implemented on `main`)

**Reviewed:** 18 November 2026 · **Backend:** Spring Boot on `http://localhost:8080` · **Prefix:** `/api`

These endpoints were transcribed from `backend/src/main/java/com/otms/controller/` and checked against `frontend/src/api/client.js`. This is **implemented API behavior**, not a list of future requirements.

## Authentication

Use `POST /api/auth/login` (no bearer token) and attach the returned token to subsequent requests:

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Dev-only seed account example (from `DataInitializer.java`; do not use these credentials for a real deployment):

```bash
curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"coordinator@otms.local","password":"Coord123!"}'
```

Response (200; token elided):

```json
{"token":"<JWT>","name":"Kiran Coordinator","role":"COORDINATOR"}
```

Roles: `ADMIN`, `COORDINATOR`, `DOCTOR`, `NURSE`, `TECHNICIAN`. Unless a stricter role is shown below, an authenticated user may call the endpoint. JWTs are stateless and expire; **refresh and revoke endpoints are not implemented on `main`**.

## Endpoint inventory

| Method | Path | Who | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Authenticate and obtain JWT |
| POST | `/api/auth/register` | Public | **501 Not Implemented**: self-registration disabled |
| GET | `/api/staff?role=DOCTOR` | Authenticated | List staff; optional case-insensitive `role` filter |
| GET | `/api/theatres?includeInactive=true` | Authenticated | List theatres; inactive hidden unless requested |
| PATCH | `/api/theatres/{id}/status` | ADMIN, COORDINATOR | Set theatre `active` flag |
| GET | `/api/equipment?includeUnavailable=true` | Authenticated | List equipment; unavailable hidden unless requested |
| PATCH | `/api/equipment/{id}/status` | ADMIN, TECHNICIAN | Set equipment `available` flag |
| GET | `/api/surgeries` | Authenticated | List surgeries; optional `date`, `theatreId`, `status` |
| GET | `/api/surgeries/{id}` | Authenticated | Get one surgery |
| POST | `/api/surgeries` | ADMIN, COORDINATOR | Create surgery; 201 on success |
| PUT | `/api/surgeries/{id}` | ADMIN, COORDINATOR | Edit active surgery |
| PATCH | `/api/surgeries/{id}/status` | ADMIN, COORDINATOR, DOCTOR | Change surgery status |
| GET | `/api/surgeries/emergencies` | Authenticated | Active emergency surgeries |
| GET | `/api/surgeries/{id}/impact` | Authenticated | Non-emergency cases impacted by an emergency |
| POST | `/api/surgeries/{id}/reschedule` | ADMIN, COORDINATOR | Move case after resource conflict validation |
| GET | `/api/notifications` | Authenticated | Latest 50 notifications |
| PATCH | `/api/notifications/{id}/read` | Authenticated | Mark a notification as read |

**Important:** the current theatre mutation path is `PATCH /api/theatres/{id}/status`, not `/availability`. The frontend has been updated to use `/status`. Older test-case documentation still contains the retired path.

## Key request and response shapes

### Surgery request — POST /api/surgeries and PUT /api/surgeries/{id}

```json
{
  "patientName": "Demo Patient",
  "procedureName": "Appendectomy",
  "leadSurgeonId": 3,
  "theatreId": 1,
  "nurseId": 5,
  "startTime": "2026-11-20T09:00",
  "endTime": "2026-11-20T10:30",
  "emergency": false,
  "notes": "Fictional demonstration",
  "equipmentIds": [1]
}
```

`patientName`, `procedureName`, surgeon, theatre, and dates are validated on the DTO. Although `nurseId` is nullable in the DTO/database schema, `SchedulingService` currently **requires a nurse** for new bookings. IDs are examples and must match data in your running environment. The end time must be after the start. An active resource overlap or unavailable theatre/equipment returns **409 Conflict**. A successful POST returns 201 and a surgery including IDs, names, status, `assignedNurseId`, `assignedNurseName`, `equipmentIds`, and `equipmentNames`.

Surgery statuses are `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.

### Theatre availability — PATCH /api/theatres/{id}/status

```json
{"active":false}
```

Role: **ADMIN or COORDINATOR**. Response: `{"id":1,"name":"OT-1","speciality":"General Surgery","active":false}` (example).

### Equipment availability — PATCH /api/equipment/{id}/status

```json
{"available":false}
```

Role: **ADMIN or TECHNICIAN**. Response fields: `id`, `name`, `type`, `available`.

### Surgery status — PATCH /api/surgeries/{id}/status

```json
{"status":"IN_PROGRESS"}
```

Role: **ADMIN, COORDINATOR, DOCTOR**. Status transitions are applied by `SchedulingService.updateStatus`; clients must not assume there is a strictly enforced lifecycle state machine for every transition.

### Emergency impact — GET /api/surgeries/{id}/impact

Returns a JSON array of active **non-emergency** surgeries overlapping the specified emergency's theatre or surgeon/time window. A non-emergency ID results in a validation error. Impact detection is not the same as automatic rescheduling.

### Assisted reschedule — POST /api/surgeries/{id}/reschedule

```json
{
  "theatreId": 2,
  "startTime": "2026-11-20T12:00",
  "endTime": "2026-11-20T13:30"
}
```

Role: **ADMIN or COORDINATOR**. Checks theatre, surgeon, nurse, equipment and overlapping operations; may return 409.

### Notifications

`GET /api/notifications` returns newest 50 entries with fields `id`, `type`, `message`, `surgeryId`, `createdAt`, `read`. Types include `SURGERY_SCHEDULED`, `EMERGENCY_SCHEDULED`, `SURGERY_RESCHEDULED`, `SURGERY_STATUS_CHANGED`. `PATCH /api/notifications/{id}/read` has no JSON body and returns the updated notification.

## Error responses

The backend's `GlobalExceptionHandler` returns an `ApiError` JSON structure for handled application exceptions:

```json
{
  "status": 409,
  "message": "Scheduling conflict detected: ...",
  "details": [],
  "timestamp": "2026-11-18T00:00:00Z"
}
```

Relevant outcomes: **400** invalid input, **401** missing/invalid authentication or invalid credentials, **403** forbidden role, **404** missing record, **409** scheduling conflict, **501** disabled registration. A given framework-level security response may have different formatting from `ApiError`.

## Local API smoke-test workflow

1. Start backend from `backend/`: `./mvnw spring-boot:run`.
2. Authenticate via `POST /api/auth/login`.
3. Fetch theatres and staff (`role=DOCTOR`, `role=NURSE`) to obtain valid IDs.
4. Fetch surgeries to choose a free future time.
5. Create a scheduled surgery with an assigned nurse; verify 201.
6. Retry an overlapping theatre/surgeon/nurse booking; verify 409.
7. Query notifications; verify the scheduling event appears.
8. Query emergencies and impact analysis; use the provided deterministic seed scenario to demonstrate affected surgery and assisted reschedule.

The repository also provides `docs/reports/system-validation.postman_collection.json`, but the collection and its evidence should be rerun against **current `main`** before claiming full system validation. No dedicated OpenAPI/Swagger generator is configured in the current backend.

## Security and deployment caveats

- H2 is in-memory by default; persistent PostgreSQL requires the `prod` profile and database environment variables.
- Current default JWT secret is marked development-only; override with `JWT_SECRET` before deployment.
- The code currently uses localStorage for JWT on the frontend. Do not put real patient data in the demo.
- Full audit-event endpoint, user self-registration, token refresh/revoke, and login lockout/rate limiting **are not present on `main`**.
