CREATE TABLE "user" (
    id          UUID PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    UNIQUE (first_name, last_name)
);

CREATE TABLE course (
    id         UUID PRIMARY KEY,
    title      VARCHAR(100) NOT NULL,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NOT NULL
);

CREATE TABLE user_course (
    id             UUID PRIMARY KEY,
    user_id        UUID      NOT NULL REFERENCES "user"(id),
    course_id      UUID      NOT NULL REFERENCES course(id),
    subscribed_at  TIMESTAMP DEFAULT now(),
    UNIQUE (user_id, course_id)
);
