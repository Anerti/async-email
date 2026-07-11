package com.async.mail.entity;

import com.async.mail.entity.enums.UserRole;

import java.util.UUID;

public record User(UUID id, String firstName, String lastName, String username, String email, UserRole role) {}
