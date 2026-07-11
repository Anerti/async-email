# Async Email

## Stack

Java 21 · Spring Boot 3.2.2 · PostgreSQL · JPA (Hibernate) · AWS Lambda/SQS/SES/EventBridge · Gradle 8.5 · Lombok · OpenAPI (codegen) · JaCoCo · TestContainers

## Project structure

```
src/main/java/com/async/mail/
├── PojaApplication.java              -- entry point
├── entity/                           -- domain records (User, Course, UserCourse)
├── repository/model/                 -- JPA entities (JUser, JCourse, JUserCourse)
├── repository/                       -- JPA repositories (JUserRepository, AuthRepository)
├── exception/                        -- GlobalExceptionHandler + typed exceptions
├── mail/                             -- SES mailer, email verification, attachments
├── config/
│   ├── SecurityConfig.java           -- HTTP security filter chain
│   ├── JwtAuthenticationFilter.java  -- JWT extraction & validation filter
│   └── JwtTokenProvider.java         -- Token generation/validation
├── mapper/
│   └── UserMapper.java               -- JUser → UserResponse
├── validator/
│   ├── AuthValidator.java            -- SignUpRequest validation rules
│   └── GeneralValidator.java         -- Shared validation utilities
├── endpoint/
│   ├── rest/controller/
│   │   ├── health/                   -- PingController, HealthEmailController
│   │   ├── AuthController.java       -- POST /auth/signup
│   │   ├── HelloWorldController      -- /hello (async email trigger)
│   │   ├── SubscribeController       -- POST /users/{userId}/courses/{courseId}
│   │   └── dto/                      -- SignUpRequest, AuthResponse, UserResponse, …
│   ├── event/model/                  -- PojaEvent, SendEmailRequested
│   ├── event/consumer/               -- EventConsumer, EventServiceInvoker
│   └── event/                        -- EventProducer, EventConf, EventStack
├── service/
│   ├── event/                        -- SendEmailRequestedService (consumer)
│   ├── AuthService.java              -- signUp logic with validation + persistence
│   └── SubscribeService              -- subscription logic with async email
├── file/hash/                        -- FileHash algorithm + model
├── file/zip/                         -- File type detection via Tika
├── handler/                          -- LambdaHandler, MailboxEventHandler (AWS)
├── concurrency/                      -- ThreadRenamer, Workers
├── datastructure/                    -- ListGrouper
└── conf/                             -- test config classes

src/test/java/com/async/mail/
├── service/auth/
│   └── AuthServiceSignupTest.java    -- 32 service-layer signup tests
├── endpoint/rest/controller/
│   └── AuthControllerSignupTest.java -- 32 controller-layer signup tests
└── conf/                             -- FacadeIT, EventConf, …

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

Spring Boot REST API with async email capabilities (SES), backed by PostgreSQL. JWT-based auth via servlet filter chain. OpenAPI-first (spec → generated code into `build/`). Deployed as AWS Lambda via `aws-serverless-java-container`.

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/auth/signup` | Register a new user account | No |
| POST | `/auth/login` | Authenticate and receive a JWT token | No |
| POST | `/users/{userId}/courses/{courseId}` | Subscribe a user to a course (sends async email) | JWT |
| GET | `/hello?to=&subject=&htmlBody=` | Produces `SendEmailRequested` event → async email via SES | No |
| GET | `/ping` | Health check | No |
| GET | `/health/email?to=` | Synchronous SES email test (5 variants) | No |

> **Status:** `/auth/signup` is fully implemented (controller + service + validator + JWT response).  
> `/auth/login` and the remaining JWT security filter wiring are defined in the OpenAPI spec at `doc/api.yml` but not yet implemented in Java.

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

# Run specific test classes
./gradlew test --tests "com.async.mail.service.auth.*"
./gradlew test --tests "com.async.mail.endpoint.rest.controller.*"

# Run app
./gradlew bootRun           # → http://localhost:8080

# Coverage
./gradlew test jacocoTestReport

# Format
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./format.sh
```

## Conventions

- **Records** for domain models (`entity/`), **JPA entities** for persistence (`repository/model/`)
- All IDs are UUIDs (auto-generated via `GenerationType.UUID`)
- Lombok `@Getter @Setter @NoArgsConstructor` on JPA entities
- DB schema managed externally (`schemas.sql`), not Hibernate auto-DDL
- Env-var-based config via `.env` (gitignored)
- Tests use TestContainers (no local DB needed)
- OpenAPI spec drives endpoint generation; generated code lands in `build/`
- **Testing pattern** by layer: service tests (real validators + mocked repository) and controller tests (MockMvc + mocked service) — both with `GlobalExceptionHandler` wired
- Each bash end-to-end test script is transcribed 1:1 into JUnit (32 cases per endpoint: service + controller = 64 tests)
- **Async email** via event-driven pipeline: `HelloWorldController` → `EventProducer<SendEmailRequested>` → EventBridge → SQS → `MailboxEventHandler` → `SendEmailRequestedService` → `Mailer` → SES
- **SendEmailRequested** fields: `to` (required), `subject` (optional, fallback `""`), `htmlBody` (optional, fallback `"... world!"`)
- `SendEmailRequestedService` implements `Consumer<SendEmailRequested>` — `@Service`, no `@Async`/`@EventListener`
- Subscription triggers async confirmation email via the same event pipeline

## Common pitfalls

- System default JDK is 26. `gradlew` auto-detects JDK 21 at `~/.jdks/ms-21.0.11` — just run `./gradlew` directly. Only `format.sh` needs explicit `JAVA_HOME`:
  ```bash
  JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./format.sh
  ```
- `user` is a reserved SQL keyword — always quoted as `"user"`
- After the Poja deployment bot runs, `build.gradle` can lose custom deps (JPA, Lombok)
- JaCoCo coverage verification runs after every test; exclude generated code via `**/gen/**`
