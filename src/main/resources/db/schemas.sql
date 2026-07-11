DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'CUSTOMER');
  END IF;
END
$$;

CREATE TABLE "user" (
    id          UUID PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL DEFAULT 'CUSTOMER',
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
    user_id        UUID      NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    course_id      UUID      REFERENCES course(id) ON DELETE SET NULL,
    subscribed_at  TIMESTAMP DEFAULT now(),
    UNIQUE (user_id, course_id)
);
