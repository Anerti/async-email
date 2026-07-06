package com.async.mail.entity;

import java.time.Instant;
import java.util.UUID;

public record Course(
    UUID id,
    String title,
    Instant startDate,
    Instant endDate) {
}
