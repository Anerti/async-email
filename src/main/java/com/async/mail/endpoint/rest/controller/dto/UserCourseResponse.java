package com.async.mail.endpoint.rest.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record UserCourseResponse(UUID id, UUID userId, UUID courseId, Instant subscribedAt) {}
