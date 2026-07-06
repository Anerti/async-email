package com.async.mail.entity;

import java.time.Instant;
import java.util.UUID;

public record UserCourse(
    UUID id,
    User user,
    Course course,
    Instant subscribedAt) {
}
