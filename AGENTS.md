# Async Email

## Stack

Java 21 · Spring Boot 3.2.2 · PostgreSQL · JPA (Hibernate) · AWS Lambda/SQS/SES/EventBridge · Gradle 8.5 · Lombok · OpenAPI (codegen) · JaCoCo · TestContainers

## Project structure

```
src/main/java/com/async/mail/
├── PojaApplication.java           -- entry point
├── entity/                        -- domain records (User, Course, UserCourse)
├── repository/model/              -- JPA entities (JUser, JCourse, JUserCourse)
├── exception/                     -- GlobalExceptionHandler + typed exceptions
├── mail/                          -- SES mailer, email verification, attachments
├── endpoint/                      -- REST controllers + EndpointConf
│   └── rest/controller/health/    -- PingController, HealthEmailController
├── file/hash/                     -- FileHash algorithm + model
├── file/zip/                      -- File type detection via Tika
├── handler/                       -- LambdaHandler (AWS)
├── concurrency/                   -- ThreadRenamer
├── datastructure/                 -- ListGrouper
└── conf/                          -- test config classes

src/main/resources/
├── db/
│   ├── schemas.sql                -- DDL (user, course, user_course)
│   ├── seed_user.sql              -- seed data: users
│   └── seed_course.sql            -- seed data: courses
└── application.properties         -- datasource config (env-var based)
```

## Architecture

Spring Boot REST API with async email capabilities (SES), backed by PostgreSQL. Security via JWT filter. OpenAPI-first (spec → generated code into `build/`). Deployed as AWS Lambda via `aws-serverless-java-container`.

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

## Common pitfalls

- `format.sh` requires JDK 21 — breaks with JDK 26
- `user` is a reserved SQL keyword — always quoted as `"user"`
- After the Poja deployment bot runs, `build.gradle` can lose custom deps (JPA, Lombok)
- JaCoCo coverage verification runs after every test; exclude generated code via `**/gen/**`
