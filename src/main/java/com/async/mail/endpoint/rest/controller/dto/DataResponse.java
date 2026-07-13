package com.async.mail.endpoint.rest.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record DataResponse(UUID id, String filename, String email, Instant createdAt) {}
