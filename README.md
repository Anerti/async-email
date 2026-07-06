# Async Email

Spring Boot REST API for asynchronous email handling via AWS SES.

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

---

*Java 21 · Spring Boot 3.2.2 · PostgreSQL · AWS Lambda/SQS/SES · Gradle · Lombok*
