package com.async.mail.endpoint.rest.controller;

import com.async.mail.config.JwtTokenProvider;
import com.async.mail.endpoint.rest.controller.dto.AuthResponse;
import com.async.mail.endpoint.rest.controller.dto.LoginRequest;
import com.async.mail.endpoint.rest.controller.dto.SignUpRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenProvider tokenProvider;
  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) {
    UserResponse userResponse = authService.signUp(request);

    String token =
        tokenProvider.generateToken(userResponse.id().toString(), userResponse.role().name());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(AuthResponse.builder().token(token).user(userResponse).build());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> logIn(@RequestBody LoginRequest request) {
    UserResponse userResponse = authService.logIn(request);

    String token =
        tokenProvider.generateToken(userResponse.id().toString(), userResponse.role().name());

    return ResponseEntity.status(HttpStatus.OK)
        .body(AuthResponse.builder().token(token).user(userResponse).build());
  }
}
