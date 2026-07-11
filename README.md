# Async Email

Spring Boot REST API for course subscription and asynchronous email handling via AWS SES.

## Stack

Java 21 · Spring Boot 3.2.2 · PostgreSQL · JPA (Hibernate) · AWS Lambda/SQS/SES/EventBridge · Gradle 8.5 · Lombok · OpenAPI (codegen) · JaCoCo · TestContainers

## Prerequisites

- Java 21
- PostgreSQL 14+
- Gradle (use `./gradlew`)

## Setup

### 1. Configure `.env`

```bash
PGHOST=localhost
PGDATABASE=async_email
PGUSER=postgres
PGPASSWORD=...
PGSSLMODE=require
```

> `.env` is gitignored.

### 2. Create the database schema

```bash
psql -h $PGHOST -U $PGUSER -d $PGDATABASE -f src/main/resources/db/schemas.sql
```

### 3. Seed data (optional)

```bash
psql -h $PGHOST -U $PGUSER -d $PGDATABASE -f src/main/resources/db/seed_user.sql
psql -h $PGHOST -U $PGUSER -d $PGDATABASE -f src/main/resources/db/seed_course.sql
```

### 4. Build & run

```bash
./gradlew build -x test
./gradlew bootRun    # → http://localhost:8080
```

### 5. Run tests

```bash
./gradlew test
```

Tests use TestContainers — no local PostgreSQL needed.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/signup` | Register a new user account |
| POST | `/auth/login` | Authenticate and receive a JWT token |
| POST | `/users/{userId}/courses/{courseId}` | Subscribe a user to a course (requires JWT) |
| GET | `/hello?to=&subject=&htmlBody=` | Produces `SendEmailRequested` event → async email via SES |
| GET | `/ping` | Health check |
| GET | `/health/email?to=` | Synchronous SES email test |

## API spec

OpenAPI 3.0.3 spec is at [`doc/api.yml`](doc/api.yml). It drives code generation into `build/`.

---

*Java 21 · Spring Boot 3.2.2 · PostgreSQL · AWS Lambda/SQS/SES · Gradle · Lombok*
