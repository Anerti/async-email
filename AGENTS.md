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
│   ├── JwtTokenProvider.java         -- Token generation/validation
│   ├── S3Conf.java                   -- S3 client + presigner beans
│   └── ResourcesAccessRules.java     -- Role-based resource access checks
├── mapper/
│   ├── UserMapper.java               -- JUser → UserResponse
│   └── CourseMapper.java             -- JCourse → CourseResponse
├── validator/
│   ├── AuthValidator.java            -- SignUpRequest validation rules
│   └── GeneralValidator.java         -- Shared validation utilities
├── endpoint/
│   ├── rest/controller/
│   │   ├── health/                   -- PingController, HealthEmailController
│   │   ├── AuthController.java       -- POST /auth/signup
│   │   ├── CourseController.java     -- GET /courses (filtered, paginated)
│   │   ├── HelloWorldController      -- /hello (async email trigger)
│   │   ├── SubscribeController       -- POST /users/{userId}/courses/{courseId}
│   │   └── dto/                      -- SignUpRequest, AuthResponse, UserResponse, CourseResponse, CourseListResponse, Meta
│   ├── event/model/                  -- PojaEvent, SendEmailRequested
│   ├── event/consumer/               -- EventConsumer, EventServiceInvoker
│   └── event/                        -- EventProducer, EventConf, EventStack
├── service/
│   ├── event/                        -- SendEmailRequestedService (consumer)
│   ├── AuthService.java              -- signUp + logIn with validation + persistence
│   ├── CourseService.java           -- filtered/paginated course listing
│   ├── SubscribeService              -- subscription logic with async email
│   ├── InvoiceService.java           -- HTML → PDF invoice generation
│   ├── QrCodeService.java            -- QR code data URI generation
│   └── S3Service.java                -- Upload invoices to S3, presigned download URLs
├── file/hash/                        -- FileHash algorithm + model
├── file/zip/                         -- File type detection via Tika
├── handler/                          -- LambdaHandler, MailboxEventHandler (AWS)
├── concurrency/                      -- ThreadRenamer, Workers
├── datastructure/                    -- ListGrouper
└── conf/                             -- test config classes

src/test/java/com/async/mail/
├── service/
│   ├── auth/
│   │   ├── AuthServiceSignupTest.java    -- 32 service-layer signup tests
│   │   └── AuthServiceLoginTest.java     -- service-layer login tests
│   └── CourseServiceTest.java           -- service-layer course listing tests
├── endpoint/rest/controller/
│   ├── AuthControllerSignupTest.java     -- 32 controller-layer signup tests
│   ├── AuthControllerLoginTest.java      -- controller-layer login tests
│   └── CourseControllerTest.java        -- controller-layer course listing tests
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

script/
├── subscribe/test_subscribe.sh       -- 10 E2E subscribe cases
└── courses/test_list_courses.sh      -- E2E course listing cases
```

## Architecture

Spring Boot REST API with async email capabilities (SES), backed by PostgreSQL. JWT-based auth via servlet filter chain. OpenAPI-first (spec → generated code into `build/`). Deployed as AWS Lambda via `aws-serverless-java-container`.

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/auth/signup` | Register a new user account | No |
| POST | `/auth/login` | Authenticate and receive a JWT token | No |
| GET | `/courses` | List courses with filters and pagination | No |
| POST | `/users/{userId}/courses/{courseId}` | Subscribe a user to a course (sends async email + invoice + QR) | JWT |
| GET | `/hello?to=&subject=&htmlBody=` | Produces `SendEmailRequested` event → async email via SES | No |
| GET | `/ping` | Health check | No |
| GET | `/health/email?to=` | Synchronous SES email test (5 variants) | No |

> **Status:** `/auth/signup`, `/auth/login`, and `GET /courses` are fully implemented (controller + service + tests).  
> Subscription (`POST /users/{userId}/courses/{courseId}`) triggers an async confirmation email with invoice PDF (S3) and QR code attachment.

## Domain entities

| Entity | Table | Purpose |
|--------|-------|---------|
| `User` (record) / `JUser` (JPA) | `"user"` | System users with unique username/email |
| `Course` (record) / `JCourse` (JPA) | `course` | Courses with start/end dates |
| `UserCourse` (record) / `JUserCourse` (JPA) | `user_course` | Many-to-many junction with subscribed_at |

## Common commands

```bash
# Build (skip tests)
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew build -x test

# Compile only
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew compileJava

# Run tests
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew test

# Run specific test classes
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew test --tests "com.async.mail.service.auth.*"
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew test --tests "com.async.mail.endpoint.rest.controller.*"

# Run app
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew bootRun           # → http://localhost:8080

# Coverage
JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./gradlew test jacocoTestReport

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
- **Subscribe test script** at `script/subscribe/test_subscribe.sh` — 10 end-to-end curl/curlie cases
- **Course test script** at `script/courses/test_list_courses.sh` — E2E cases for filtered/paginated listing
- **Invoice + QR**: subscription triggers a confirmation email with an invoice PDF (Flying Saucer) uploaded to S3 and a QR code data URI
- **S3Service** uploads invoices and generates presigned download URLs (7-day expiry)

## Common pitfalls

- System default JDK is 26. `gradle.properties` no longer pins `org.gradle.java.home` (Gradle 8.5 rejects it when the wrapper runs on JDK 26). Always prefix `./gradlew` commands with `JAVA_HOME=$HOME/.jdks/ms-21.0.11` — see **Common commands** above.
- Only `format.sh` needs explicit `JAVA_HOME` as well:
  ```bash
  JAVA_HOME=$HOME/.jdks/ms-21.0.11 ./format.sh
  ```
- If the Poja bot rewrites `build.gradle` and drops custom deps, it may also drop `gradle.properties` — recreate it if `compileJava` fails with a Lombok `NoSuchFieldException`.
- `user` is a reserved SQL keyword — always quoted as `"user"`
- After the Poja deployment bot runs, `build.gradle` can lose custom deps (spring-data-jpa, postgresql, spring-security, jjwt, s3, flying-saucer, zxing, bcprov) — compare with `git diff HEAD build.gradle` and restore them
- `.env` and `.obsidian/` entries in `.gitignore` are also removed by pojabot — re-add them after each deployment
- JaCoCo coverage verification runs after every test; exclude generated code via `**/gen/**`
