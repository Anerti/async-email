package com.async.mail.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.async.mail.config.JwtTokenProvider;
import com.async.mail.endpoint.rest.controller.dto.LoginRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.entity.enums.UserRole;
import com.async.mail.exception.GlobalExceptionHandler;
import com.async.mail.exception.UnauthorizedException;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginTest {

  @Mock AuthService authService;
  @Mock JwtTokenProvider tokenProvider;

  MockMvc mockMvc;
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(tokenProvider, authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  // ── 200 OK ──────────────────────────────────────────────────

  @Test
  void test1_200_valid_credentials() throws Exception {
    var userId = UUID.randomUUID();
    var userResponse =
        new UserResponse(userId, "John", "Doe", "john_doe", "john.doe@test.com", UserRole.CUSTOMER);

    when(authService.logIn(any(LoginRequest.class))).thenReturn(userResponse);
    when(tokenProvider.generateToken(userId.toString(), "CUSTOMER")).thenReturn("jwt-token");

    var request = new LoginRequest("john_doe", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.user.email").value("john.doe@test.com"))
        .andExpect(jsonPath("$.user.firstName").value("John"))
        .andExpect(jsonPath("$.user.lastName").value("Doe"))
        .andExpect(jsonPath("$.user.id").value(userId.toString()))
        .andExpect(jsonPath("$.user.username").value("john_doe"))
        .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
  }

  // ── 401 UNAUTHORIZED ─────────────────────────────────────────

  @Test
  void test2_401_invalid_credentials() throws Exception {
    when(authService.logIn(any(LoginRequest.class)))
        .thenThrow(new UnauthorizedException("Invalid credentials."));

    var request = new LoginRequest("unknown", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
  }

  // ── 422 UNPROCESSABLE ENTITY ─────────────────────────────────

  @Test
  void test3_422_null_username() throws Exception {
    when(authService.logIn(any(LoginRequest.class)))
        .thenThrow(new UnprocessableEntityException("username is required and cannot be blank."));

    var request = new LoginRequest(null, "TestPass1!");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void test4_422_blank_username() throws Exception {
    when(authService.logIn(any(LoginRequest.class)))
        .thenThrow(new UnprocessableEntityException("username is required and cannot be blank."));

    var request = new LoginRequest("", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void test5_422_null_password() throws Exception {
    when(authService.logIn(any(LoginRequest.class)))
        .thenThrow(new UnprocessableEntityException("password is required and cannot be blank."));

    var request = new LoginRequest("john_doe", null);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void test6_422_blank_password() throws Exception {
    when(authService.logIn(any(LoginRequest.class)))
        .thenThrow(new UnprocessableEntityException("password is required and cannot be blank."));

    var request = new LoginRequest("john_doe", "");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
  }
}
