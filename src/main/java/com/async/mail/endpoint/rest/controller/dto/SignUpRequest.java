package com.async.mail.endpoint.rest.controller.dto;

public record SignUpRequest(
    String firstName,
    String lastName,
    String username,
    String email,
    String password,
    String confirmPassword) {}
