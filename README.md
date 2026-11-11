# Operation Theatre Management System (OTMS)

## Project Overview

The **Operation Theatre Management System (OTMS)** is a software engineering mini-project designed to improve the scheduling and coordination of hospital operation theatres. It provides a centralized system for managing surgeries, medical staff availability, theatre schedules, surgical equipment, emergency cases, and resource utilization.

The system aims to reduce scheduling conflicts, prevent double-booking of operation theatres and critical resources, prioritize emergency surgeries, and improve overall operational efficiency.

## Objectives

- Manage surgery records and operation theatre schedules.
- Check the availability of doctors, nurses, theatres, and surgical equipment.
- Detect scheduling conflicts before confirming surgeries.
- Prioritize emergency surgeries and support rescheduling of affected cases.
- Provide role-based access for authorized hospital personnel.
- Generate theatre and resource utilization reports.
- Maintain audit logs and protect sensitive patient information.

## Key Features

- **Authentication and Authorization:** Secure login and role-based access control.
- **Surgery Management:** Create, view, and update surgery records.
- **Theatre Scheduling:** Allocate available theatre slots based on scheduling requirements.
- **Conflict Detection:** Identify overlapping surgeries and unavailable resources.
- **Emergency Prioritization:** Prioritize emergency cases and identify affected schedules.
- **Resource Management:** Track staff, theatre, and equipment availability.
- **Notifications:** Inform relevant users about schedule changes and conflicts.
- **Reporting and Monitoring:** Support utilization reporting and audit logging.

## Intended Users

- Hospital Administrators
- Operation Theatre Coordinators
- Doctors and Surgeons
- Nurses
- Operation Theatre Technicians
- Hospital Management

## Technology Stack

The proposed technology stack, as documented in the Software Requirements Specification and Software Architecture and Design Specification, includes:

- **Frontend:** HTML, CSS, JavaScript / React
- **Backend:** Java with Spring Boot
- **API:** REST APIs
- **Database:** MySQL or PostgreSQL
- **Authentication:** JWT or secure session-based authentication
- **Communication Security:** TLS 1.2 or higher
- **Testing Tools:** Postman, JMeter, and Selenium
- **Design Tools:** PlantUML and draw.io
- **Version Control:** Git and GitHub

## Project Documentation

The repository currently contains the following project documents:

1. **Software Requirements Specification (SRS)** — Defines functional and non-functional requirements, system features, interfaces, security requirements, and acceptance criteria.
2. **Software Architecture and Design Specification (SAD)** — Describes the system architecture, components, design decisions, UML diagrams, API specifications, and security architecture.
3. **Software Test Plan (STP)** — Defines the testing scope, strategy, test environment, responsibilities, test cases, acceptance criteria, and test deliverables.

## Development and Collaboration

The project uses Git and GitHub for collaborative development and version control.

- Each feature or change must be developed in a separate branch.
- Changes must be submitted through Pull Requests.
- At least one teammate must review and approve each Pull Request.
- Automated CI checks must pass before merging.
- Direct, unverified pushes to the `main` branch are not permitted.
- Team members should pull the latest changes from `main` after merges.
- Contributions should be meaningful, balanced, and properly tested.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the developer workflow (branches, tests, pull
requests, reviews, and syncing `main`) and [docs/git-workflow.md](docs/git-workflow.md)
for how the rules are enforced by CI, hooks, and branch protection.

### PostgreSQL development database\n\nThe default profile uses in-memory H2 and applies the same Flyway migrations automatically. To run with PostgreSQL:\n\n```bash\ndocker compose up -d postgres\ncd backend\nDB_URL=jdbc:postgresql://localhost:5432/otms DB_USERNAME=otms DB_PASSWORD=otms_dev_password JWT_SECRET=replace-this-with-a-long-random-secret ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod\n```\n\nFlyway applies versioned SQL migrations on startup; Hibernate validates the resulting schema and does not create or alter tables. The PostgreSQL CI job runs the backend test suite against a real PostgreSQL service.\n\n### Quick start for contributors

```bash
git clone https://github.com/suravii28/Operation-Theatre-Management-System.git
cd Operation-Theatre-Management-System
./scripts/setup-dev.sh
```

## Project Scope

OTMS focuses on the operational coordination of surgeries and operation theatre resources. It does not perform medical diagnosis or implement clinical treatment processes. Integration with external hospital systems is limited to defined interfaces and required data exchanges.

## Project Status

**Current Phase:** Application implementation and integration testing

The repository includes a React/Vite frontend and Spring Boot backend with JWT authentication, role-based authorization, theatre/surgery scheduling, conflict detection, emergency workflows, notifications and reporting. Features are delivered through reviewed pull requests. PostgreSQL is supported through Flyway migrations; H2 remains the default local development database.

## Roles Assigned

| Role                                   | Name      |
|----------------------------------------|-----------|
| QA Lead                                | Sudhiksha |
| Development Lead                       | Suravi    |
| OT Coordinator / Domain Representative | Praneetha |
| Product Owner                          | Shesanth  |

---

*Academic Software Engineering Mini-Project — Operation Theatre Management System (OTMS).*
