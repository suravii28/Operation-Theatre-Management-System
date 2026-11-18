# OTMS — implemented architecture (18 November 2026)

Source of truth: repository `main` at the time of this review. This diagram describes **implemented components**, not aspirational requirements.

## System/component diagram

```mermaid
flowchart TB
    U["Hospital staff<br/>Admin · Coordinator · Doctor · Nurse · Technician"]
    subgraph FE["Frontend — React + Vite (port 5173 in dev)"]
        UI["React Router pages<br/>Login · Dashboard · Surgeries · Theatres<br/>Emergencies · Notifications · Reports"]
        CLIENT["API client<br/>JWT in localStorage<br/>Bearer header"]
        UI --> CLIENT
    end
    subgraph BE["Backend — Spring Boot (port 8080)"]
        FILTER["Spring Security<br/>JWT authentication filter<br/>Role-based method guards"]
        CTRL["REST controllers<br/>Auth · Staff · Theatre · Surgery<br/>Equipment · Notification"]
        SVC["SchedulingService<br/>conflict checks / emergency impact<br/>nurse & equipment validation<br/>pessimistic locks"]
        NOTIFY["NotificationService<br/>persist scheduling events"]
        REPOS["Spring Data JPA repositories"]
        FILTER --> CTRL
        CTRL --> SVC
        CTRL --> NOTIFY
        CTRL --> REPOS
        SVC --> NOTIFY
        SVC --> REPOS
        NOTIFY --> REPOS
    end
    subgraph DB["Persistence"]
        H2["H2 in-memory database<br/>default development profile"]
        PG["PostgreSQL 16<br/>production profile / CI service"]
        FLY["Flyway V1–V3 migrations"]
    end
    CI["GitHub Actions CI<br/>Maven · Vitest/lint · Postgres/Flyway<br/>test guard · docs checks"]
    U --> UI
    CLIENT -->|"HTTP /api/* (Vite proxy in dev)"| FILTER
    REPOS -->|"JPA"| H2
    REPOS -.->|"prod profile"| PG
    FLY --> H2
    FLY --> PG
    CI -.-> FE
    CI -.-> BE
    CI -.-> PG
```

## Core data model

```mermaid
erDiagram
    USER_ACCOUNT ||--o{ SURGERY : "lead surgeon"
    USER_ACCOUNT ||--o{ SURGERY : "assigned nurse (optional in schema)"
    THEATRE ||--o{ SURGERY : "scheduled in"
    SURGERY ||--o{ NOTIFICATION : "event references ID"
    SURGERY ||--o{ SURGERY_EQUIPMENT : uses
    EQUIPMENT ||--o{ SURGERY_EQUIPMENT : assigned
    USER_ACCOUNT {
        bigint id
        string email
        string name
        string role
        boolean active
    }
    THEATRE {
        bigint id
        string name
        string speciality
        boolean active
    }
    SURGERY {
        bigint id
        string patientName
        string procedureName
        datetime startTime
        datetime endTime
        string status
        boolean emergency
    }
    EQUIPMENT {
        bigint id
        string name
        string type
        boolean available
    }
    NOTIFICATION {
        bigint id
        string type
        bigint surgeryId
        boolean read
        datetime createdAt
    }
    SURGERY_EQUIPMENT {
        bigint surgeryId
        bigint equipmentId
    }
```

*Conceptual ER diagram*: notifications store `surgeryId`, not necessarily a database-enforced foreign key. Nurse assignment was introduced in Flyway V3; the column is nullable in the schema, while current scheduling service logic requires an assigned nurse on new surgery requests.

## Example scheduling sequence

```mermaid
sequenceDiagram
    actor Coordinator
    participant React as React UI
    participant API as SurgeryController
    participant Service as SchedulingService
    participant DB as Repositories / Database
    participant Notify as NotificationService
    Coordinator->>React: Enter surgery, surgeon, nurse, theatre, slot
    React->>API: POST /api/surgeries + Bearer JWT
    API->>Service: schedule(validated request)
    Service->>DB: Lock theatre, surgeon, nurse, equipment
    Service->>DB: Query overlapping active surgeries
    alt Resource conflict / unavailable
        Service-->>API: SchedulingConflictException
        API-->>React: HTTP 409 + ApiError
    else Valid slot
        Service->>DB: Save surgery
        Service->>Notify: Record scheduled / emergency event
        Notify->>DB: Save notification
        API-->>React: HTTP 201 + surgery
    end
```

## Known boundaries

- **Not in `main`:** full audit-event persistence/query API, refresh tokens, account lockout and rate limiting (corresponding feature PRs were closed without merge). Do not present these as implemented features.
- **Frontend:** React/Vite dev server proxies `/api` to `localhost:8080`; this is not a production deployment diagram.
- **Authentication:** login returns a signed JWT; the frontend holds it in `localStorage`. The server checks the JWT and method-level roles. Self-registration is intentionally unavailable (placeholder responds 501).
- **Availability:** current application checks active theatre/surgeon/nurse and equipment plus overlapping bookings; it does **not** model comprehensive staff rosters, shift hours, or leave calendars.
- **Emergency workflow:** the app provides impact analysis and assisted rescheduling; do not imply that every affected surgery is automatically rescheduled.
- **Reporting:** utilization/CSV/print views are calculated on the frontend from the existing surgery APIs; no dedicated report controller exists in current `main`.
- **CI:** `main` passed the most recent observed CI run, while draft PR #53 failed frontend lint; do not describe the draft additions as merged.

Related files: `frontend/src/App.jsx`, `frontend/src/api/client.js`, `backend/src/main/java/com/otms/controller/`, `backend/src/main/java/com/otms/service/SchedulingService.java`, `backend/src/main/resources/db/migration/`, `.github/workflows/ci-cd.yml`.
