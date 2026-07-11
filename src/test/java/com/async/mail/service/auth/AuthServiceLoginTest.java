package com.async.mail.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.async.mail.endpoint.rest.controller.dto.LoginRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.entity.enums.UserRole;
import com.async.mail.exception.UnauthorizedException;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.mapper.UserMapper;
import com.async.mail.repository.AuthRepository;
import com.async.mail.repository.model.JUser;
import com.async.mail.service.AuthService;
import com.async.mail.validator.AuthValidator;
import com.async.mail.validator.GeneralValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

  @Mock AuthRepository authRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock UserMapper userMapper;

  AuthService authService;

  @BeforeEach
  void setUp() {
    var generalValidator = new GeneralValidator();
    var authValidator = new AuthValidator(generalValidator);
    authService = new AuthService(authRepository, passwordEncoder, userMapper, authValidator);
  }

  @Nested
  class Success200 {

    @Test
    void test1_valid_credentials() {
      var request = new LoginRequest("john_doe", "TestPass1!");
      var jUser = new JUser();
      jUser.setPassword("encoded-password");
      var userResponse =
          new UserResponse(
              UUID.randomUUID(), "John", "Doe", "john_doe", "john.doe@test.com", UserRole.CUSTOMER);

      when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(jUser));
      when(passwordEncoder.matches("TestPass1!", "encoded-password")).thenReturn(true);
      when(userMapper.toResponse(jUser)).thenReturn(userResponse);

      var result = authService.logIn(request);

      assertEquals(userResponse, result);
      verify(authRepository).findByUsername("john_doe");
    }
  }

  @Nested
  class Unauthorized401 {

    @Test
    void test2_unknown_username() {
      var request = new LoginRequest("unknown", "TestPass1!");

      when(authRepository.findByUsername("unknown")).thenReturn(Optional.empty());

      var ex = assertThrows(UnauthorizedException.class, () -> authService.logIn(request));
      assertTrue(ex.getMessage().contains("Invalid credentials"));
    }

    @Test
    void test3_wrong_password() {
      var request = new LoginRequest("john_doe", "WrongPass1!");
      var jUser = new JUser();
      jUser.setPassword("encoded-password");

      when(authRepository.findByUsername("john_doe")).thenReturn(Optional.of(jUser));
      when(passwordEncoder.matches("WrongPass1!", "encoded-password")).thenReturn(false);

      var ex = assertThrows(UnauthorizedException.class, () -> authService.logIn(request));
      assertTrue(ex.getMessage().contains("Invalid credentials"));
    }
  }

  @Nested
  class ValidationErrors422 {

    @Test
    void test4_null_username() {
      var request = new LoginRequest(null, "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.logIn(request));
      assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void test5_blank_username() {
      var request = new LoginRequest("", "TestPass1!");
      assertThrows(UnprocessableEntityException.class, () -> authService.logIn(request));
    }

    @Test
    void test6_null_password() {
      var request = new LoginRequest("john_doe", null);
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.logIn(request));
      assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void test7_blank_password() {
      var request = new LoginRequest("john_doe", "");
      assertThrows(UnprocessableEntityException.class, () -> authService.logIn(request));
    }
  }
}
