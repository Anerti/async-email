package com.async.mail.entity;

import java.time.Instant;
import java.util.UUID;

public record Data(UUID id, String filename, String email, Instant createdAt) {}
