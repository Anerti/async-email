package com.async.mail.endpoint.rest.controller.dto;

import com.async.mail.entity.enums.UserRole;

public record UserResponse(
    java.util.UUID id,
    String firstName,
    String lastName,
    String username,
    String email,
    UserRole role) {}
