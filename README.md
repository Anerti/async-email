# Async Email

Spring Boot REST API for course subscription and asynchronous email handling via AWS SES.

## Stack

Java 21 · Spring Boot 3.2.2 · PostgreSQL · JPA (Hibernate) · AWS Lambda/SQS/SES/EventBridge · Gradle 8.5 · Lombok · OpenAPI (codegen) · JaCoCo · TestContainers · JUnit 5 · Mockito 5

## Prerequisites

- Java 21 (system default is JDK 26 — prefix `./gradlew` with `JAVA_HOME=$HOME/.jdks/ms-21.0.11`)
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
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew build -x test
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew bootRun    # → http://localhost:8080
```

### 5. Run tests

```bash
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew test
```

Tests use TestContainers — no local PostgreSQL needed. Service and controller layers are tested separately (64 JUnit tests for `/auth/signup`, plus login tests).

### 6. Format code

```bash
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./format.sh
```

That's it — no other setup needed.

## Project layout

```text
src/main/java/com/async/mail/
├── config/               Security config, JWT filter, token provider, S3 conf
├── endpoint/rest/        AuthController, SubscribeController, HelloWorldController, …
├── service/              AuthService, SubscribeService, InvoiceService, QrCodeService, S3Service
├── validator/            AuthValidator, GeneralValidator
├── repository/           JPA repositories (AuthRepository, JUserCourseRepository, …)
├── mapper/               UserMapper (JUser → UserResponse)
└── ...

src/test/java/com/async/mail/
├── service/auth/         AuthServiceSignupTest (32), AuthServiceLoginTest
├── endpoint/rest/        AuthControllerSignupTest (32), AuthControllerLoginTest
└── conf/                 FacadeIT, EventConf, …
```

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/auth/signup` | Register a new user account | No |
| POST | `/auth/login` | Authenticate and receive a JWT token | No |
| POST | `/users/{userId}/courses/{courseId}` | Subscribe a user to a course (requires JWT) | JWT |
| GET | `/hello?to=&subject=&htmlBody=` | Produces `SendEmailRequested` event → async email via SES | No |
| GET | `/ping` | Health check | No |
| GET | `/health/email?to=` | Synchronous SES email test | No |

## Test scripts

E2E test scripts for manual verification live in `script/`:

| Script | Cases |
|--------|-------|
| [`script/subscribe/test_subscribe.sh`](script/subscribe/test_subscribe.sh) | 10 cases (happy path + auth errors + permission checks + not found + conflict) |

## Caveats

- **Poja deployment bot** (`poja[bot]`) rewrites `build.gradle` and `.gitignore`, stripping custom dependencies (spring-data-jpa, postgresql, spring-security, jjwt, s3, flying-saucer, zxing, bcprov) and removing `.env` / `.obsidian/` entries from `.gitignore`. After each deployment, run `git diff HEAD build.gradle` and restore missing lines.
- System default JDK is 26; Gradle 8.5 rejects `org.gradle.java.home` in `gradle.properties`. All `./gradlew` commands must be prefixed with `JAVA_HOME=$HOME/.jdks/ms-21.0.11`.

## API spec

OpenAPI 3.0.3 spec is at [`doc/api.yml`](doc/api.yml). It drives code generation into `build/`.

---

*Java 21 · Spring Boot 3.2.2 · PostgreSQL · AWS Lambda/SQS/SES · Gradle · Lombok*
