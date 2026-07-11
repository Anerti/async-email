# Async Email

## Stack

Java 21 · Spring Boot 3.2.2 · PostgreSQL · JPA (Hibernate) · AWS Lambda/SQS/SES/EventBridge · Gradle 8.5 · Lombok · OpenAPI (codegen) · JaCoCo · TestContainers

## Project structure

```
src/main/java/com/async/mail/
├── PojaApplication.java              -- entry point
├── entity/                           -- domain records (User, Course, UserCourse)
├── repository/model/                 -- JPA entities (JUser, JCourse, JUserCourse)
├── repository/                       -- JPA repositories (JUserRepository, …)
├── exception/                        -- GlobalExceptionHandler + typed exceptions
├── mail/                             -- SES mailer, email verification, attachments
├── endpoint/
│   ├── rest/controller/
│   │   ├── health/                   -- PingController, HealthEmailController
│   │   ├── HelloWorldController      -- /hello (async email trigger)
│   │   ├── SubscribeController       -- POST /users/{userId}/courses/{courseId}
│   │   └── dto/                      -- UserCourseResponse
│   ├── event/model/                  -- PojaEvent, SendEmailRequested
│   ├── event/consumer/               -- EventConsumer, EventServiceInvoker
│   └── event/                        -- EventProducer, EventConf, EventStack
├── service/
│   ├── event/                        -- SendEmailRequestedService (consumer)
│   └── SubscribeService              -- subscription logic with async email
├── file/hash/                        -- FileHash algorithm + model
├── file/zip/                         -- File type detection via Tika
├── handler/                          -- LambdaHandler, MailboxEventHandler (AWS)
├── concurrency/                      -- ThreadRenamer, Workers
├── datastructure/                    -- ListGrouper
└── conf/                             -- test config classes

src/main/resources/
├── db/
│   ├── schemas.sql                   -- DDL (user, course, user_course)
│   ├── seed_user.sql                 -- seed data: users
│   └── seed_course.sql               -- seed data: courses
└── application.properties            -- datasource config (env-var based)

doc/
├── api.yml                           -- OpenAPI 3.0.3 spec
└── mcd.canvas                        -- Obsidian canvas (entity diagram)
```

## Architecture

Spring Boot REST API with async email capabilities (SES), backed by PostgreSQL. Security via JWT filter (spec defined, not yet implemented in Java). OpenAPI-first (spec → generated code into `build/`). Deployed as AWS Lambda via `aws-serverless-java-container`.

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/auth/signup` | Register a new user account | No |
| POST | `/auth/login` | Authenticate and receive a JWT token | No |
| POST | `/users/{userId}/courses/{courseId}` | Subscribe a user to a course (sends async email) | JWT |
| GET | `/hello?to=&subject=&htmlBody=` | Produces `SendEmailRequested` event → async email via SES | No |
| GET | `/ping` | Health check | No |
| GET | `/health/email?to=` | Synchronous SES email test (5 variants) | No |

> **Note:** Auth endpoints (`/auth/signup`, `/auth/login`) and JWT security filter are defined in the OpenAPI spec at `doc/api.yml` but not yet implemented in Java.

## Domain entities

| Entity | Table | Purpose |
|--------|-------|---------|
| `User` (record) / `JUser` (JPA) | `"user"` | System users with unique username/email |
| `Course` (record) / `JCourse` (JPA) | `course` | Courses with start/end dates |
| `UserCourse` (record) / `JUserCourse` (JPA) | `user_course` | Many-to-many junction with subscribed_at |

## Common commands

```bash
# Build (skip tests)
./gradlew build -x test

# Compile only
./gradlew compileJava

# Run tests
./gradlew test

# Run app
./gradlew bootRun           # → http://localhost:8080

# Coverage
./gradlew test jacocoTestReport

# Format
./format.sh
```

## Conventions

- **Records** for domain models (`entity/`), **JPA entities** for persistence (`repository/model/`)
- All IDs are UUIDs (auto-generated via `GenerationType.UUID`)
- Lombok `@Getter @Setter @NoArgsConstructor` on JPA entities
- DB schema managed externally (`schemas.sql`), not Hibernate auto-DDL
- Env-var-based config via `.env` (gitignored)
- Tests use TestContainers (no local DB needed)
- OpenAPI spec drives endpoint generation; generated code lands in `build/`
- **Async email** via event-driven pipeline: `HelloWorldController` → `EventProducer<SendEmailRequested>` → EventBridge → SQS → `MailboxEventHandler` → `SendEmailRequestedService` → `Mailer` → SES
- **SendEmailRequested** fields: `to` (required), `subject` (optional, fallback `""`), `htmlBody` (optional, fallback `"... world!"`)
- `SendEmailRequestedService` implements `Consumer<SendEmailRequested>` — `@Service`, no `@Async`/`@EventListener`
- Subscription triggers async confirmation email via the same event pipeline

## Common pitfalls

- `format.sh` requires JDK 21 — breaks with JDK 26
- `user` is a reserved SQL keyword — always quoted as `"user"`
- After the Poja deployment bot runs, `build.gradle` can lose custom deps (JPA, Lombok)
- JaCoCo coverage verification runs after every test; exclude generated code via `**/gen/**`
