package com.async.mail.entity;

import java.util.UUID;

public record User(UUID id, String firstName, String lastName, String username, String email) {}
