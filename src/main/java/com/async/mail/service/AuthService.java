package com.async.mail.service;

import com.async.mail.endpoint.rest.controller.dto.LoginRequest;
import com.async.mail.endpoint.rest.controller.dto.SignUpRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.entity.enums.UserRole;
import com.async.mail.exception.ConflictException;
import com.async.mail.exception.UnauthorizedException;
import com.async.mail.mapper.UserMapper;
import com.async.mail.repository.AuthRepository;
import com.async.mail.validator.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthRepository authRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final AuthValidator authValidator;

  @Transactional
  public UserResponse signUp(SignUpRequest request) {
    authValidator.validateSignUp(request);

    String encodedPassword = passwordEncoder.encode(request.password());

    return authRepository
        .create(
            request.firstName(),
            request.lastName(),
            request.username(),
            request.email(),
            encodedPassword,
            UserRole.CUSTOMER.name())
        .map(userMapper::toResponse)
        .orElseThrow(
            () ->
                new ConflictException(
                    String.format(
                        "Username %s or email %s already taken.",
                        request.username(), request.email())));
  }

  public UserResponse logIn(LoginRequest request) {
    authValidator.validateLogin(request);

    var user =
        authRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid credentials.");
    }

    return userMapper.toResponse(user);
  }
}
