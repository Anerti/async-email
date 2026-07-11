package com.async.mail.mapper;

import com.async.mail.endpoint.rest.controller.dto.AuthResponse.AuthUser;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.repository.model.JUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(JUser user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getUsername(),
        user.getEmail(),
        user.getRole());
  }

  public AuthUser toAuthUser(UserResponse user) {
    return AuthUser.builder()
        .email(user.email())
        .firstName(user.firstName())
        .lastName(user.lastName())
        .build();
  }
}
