package com.async.mail.endpoint.rest.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
  private String token;
  private AuthUser user;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AuthUser {
    private String email;
    private String firstName;
    private String lastName;
  }
}
