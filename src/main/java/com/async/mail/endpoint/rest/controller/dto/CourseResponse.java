package com.async.mail.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CourseResponse(UUID id, String title, Instant startDate, Instant endDate, BigDecimal price) {}
