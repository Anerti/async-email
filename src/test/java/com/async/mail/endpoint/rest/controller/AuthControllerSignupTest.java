package com.async.mail.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.async.mail.config.JwtTokenProvider;
import com.async.mail.endpoint.rest.controller.dto.SignUpRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.entity.enums.UserRole;
import com.async.mail.exception.ConflictException;
import com.async.mail.exception.GlobalExceptionHandler;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.mapper.UserMapper;
import com.async.mail.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerSignupTest {

  @Mock AuthService authService;
  @Mock JwtTokenProvider tokenProvider;

  MockMvc mockMvc;
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AuthController(tokenProvider, authService, new UserMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  // ── 201 CREATED ──────────────────────────────────────────────

  @Test
  void test1_201_valid_all_fields() throws Exception {
    var userId = UUID.randomUUID();
    var userResponse =
        new UserResponse(userId, "John", "Doe", "john_doe", "john.doe@test.com", UserRole.CUSTOMER);

    when(authService.signUp(any(SignUpRequest.class))).thenReturn(userResponse);
    when(tokenProvider.generateToken(userId.toString(), "CUSTOMER")).thenReturn("jwt-token");

    var request =
        new SignUpRequest(
            "John", "Doe", "john_doe", "john.doe@test.com", "TestPass1!", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.user.email").value("john.doe@test.com"))
        .andExpect(jsonPath("$.user.firstName").value("John"))
        .andExpect(jsonPath("$.user.lastName").value("Doe"));
  }

  @Test
  void test2_201_valid_accented_names() throws Exception {
    var userId = UUID.randomUUID();
    var userResponse =
        new UserResponse(userId, "Jéan", "Doe", "jean_doe", "jean.doe@test.com", UserRole.CUSTOMER);

    when(authService.signUp(any(SignUpRequest.class))).thenReturn(userResponse);
    when(tokenProvider.generateToken(userId.toString(), "CUSTOMER")).thenReturn("jwt-token");

    var request =
        new SignUpRequest(
            "Jéan", "Doe", "jean_doe", "jean.doe@test.com", "TestPass1!", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user.firstName").value("Jéan"))
        .andExpect(jsonPath("$.user.lastName").value("Doe"));
  }

  // ── 409 CONFLICT ─────────────────────────────────────────────

  @Test
  void test3_409_duplicate_username() throws Exception {
    when(authService.signUp(any(SignUpRequest.class)))
        .thenThrow(
            new ConflictException("Username john_doe or email jane.smith@test.com already taken."));

    var request =
        new SignUpRequest(
            "Jane", "Smith", "john_doe", "jane.smith@test.com", "TestPass1!", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("CONFLICT"));
  }

  @Test
  void test4_409_duplicate_email() throws Exception {
    when(authService.signUp(any(SignUpRequest.class)))
        .thenThrow(
            new ConflictException("Username jane_smith or email john.doe@test.com already taken."));

    var request =
        new SignUpRequest(
            "Jane", "Smith", "jane_smith", "john.doe@test.com", "TestPass1!", "TestPass1!");

    mockMvc
        .perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("CONFLICT"));
  }

  // ── 422 UNPROCESSABLE ENTITY ─────────────────────────────────

  @Nested
  class LastNameErrors {

    @Test
    void test5_422_lastName_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("lastName is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", null, "tn1", "tn1@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test6_422_lastName_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("lastName is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", "", "tn2", "tn2@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test7_422_lastName_with_digits() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "lastName field contain forbidden characters. "
                      + "Only letters (a-z, A-Z, éèê), hyphen and space are allowed."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont123", "tn3", "tn3@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test8_422_lastName_too_long() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("lastName cannot be longer than 100 characters."));

      var request =
          new SignUpRequest(
              "Marie", "D".repeat(101), "tn4", "tn4@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class FirstNameErrors {

    @Test
    void test9_422_firstName_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("firstName is required and cannot be blank."));

      var request =
          new SignUpRequest(null, "Dupont", "tn5", "tn5@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test10_422_firstName_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("firstName is required and cannot be blank."));

      var request =
          new SignUpRequest("", "Dupont", "tn6", "tn6@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test11_422_firstName_with_digits() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "firstName field contain forbidden characters. "
                      + "Only letters (a-z, A-Z, éèê), hyphen and space are allowed."));

      var request =
          new SignUpRequest(
              "Marie123", "Dupont", "tn7", "tn7@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test12_422_firstName_too_long() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("firstName cannot be longer than 100 characters."));

      var request =
          new SignUpRequest(
              "M".repeat(101), "Dupont", "tn8", "tn8@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class UsernameErrors {

    @Test
    void test13_422_username_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("username is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", "Dupont", null, "tn9@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test14_422_username_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("username is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", "Dupont", "", "tn10@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test15_422_username_with_at_sign() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Username can only contain letters (a-z, A-Z), "
                      + "digits (0-9) and underscores."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "john@doe", "tn11@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test16_422_username_with_hyphen() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Username can only contain letters (a-z, A-Z), "
                      + "digits (0-9) and underscores."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "john-doe", "tn12@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test17_422_username_too_long() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("username cannot be longer than 50 characters."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "u".repeat(51), "tn13@test.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class EmailErrors {

    @Test
    void test18_422_email_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("email is required and cannot be blank."));

      var request = new SignUpRequest("Marie", "Dupont", "tn14", null, "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test19_422_email_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("email is required and cannot be blank."));

      var request = new SignUpRequest("Marie", "Dupont", "tn15", "", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test20_422_email_invalid_format() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("Invalid email format: 'not-an-email'"));

      var request =
          new SignUpRequest("Marie", "Dupont", "tn16", "not-an-email", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test21_422_email_forbidden_chars() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Invalid input for email: 'marie @mail.com' "
                      + "only a-zA-Z0-9@_.- characters are allowed."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn17", "marie @mail.com", "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test22_422_email_too_long() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("email cannot be longer than 100 characters."));

      var email = "m".repeat(92) + "@mail.com";
      var request = new SignUpRequest("Marie", "Dupont", "tn18", email, "TestPass1!", "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class PasswordErrors {

    @Test
    void test23_422_password_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("password is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", "Dupont", "tn19", "tn19@test.com", null, "TestPass1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test24_422_password_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("password is required and cannot be blank."));

      var request = new SignUpRequest("Marie", "Dupont", "tn20", "tn20@test.com", "", "");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test25_422_password_too_short() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("password must be at least 8 characters."));

      var request = new SignUpRequest("Marie", "Dupont", "tn21", "tn21@test.com", "Ab1!", "Ab1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test26_422_password_no_uppercase() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Password must contain at least one uppercase character."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn22", "tn22@test.com", "lowercase1!", "lowercase1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test27_422_password_no_lowercase() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Password must contain at least one lowercase character."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn23", "tn23@test.com", "UPPERCASE1!", "UPPERCASE1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test28_422_password_no_digit() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("Password must contain at least one digit."));

      var request =
          new SignUpRequest("Marie", "Dupont", "tn24", "tn24@test.com", "NoDigit!x", "NoDigit!x");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test29_422_password_no_special_character() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException(
                  "Password must contain at least one special character."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn25", "tn25@test.com", "NoSpecial1x", "NoSpecial1x");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class ConfirmPasswordErrors {

    @Test
    void test30_422_confirmPassword_null() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("confirmPassword is required and cannot be blank."));

      var request =
          new SignUpRequest("Marie", "Dupont", "tn26", "tn26@test.com", "TestPass1!", null);

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test31_422_confirmPassword_blank() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(
              new UnprocessableEntityException("confirmPassword is required and cannot be blank."));

      var request = new SignUpRequest("Marie", "Dupont", "tn27", "tn27@test.com", "TestPass1!", "");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test32_422_confirmPassword_mismatch() throws Exception {
      when(authService.signUp(any(SignUpRequest.class)))
          .thenThrow(new UnprocessableEntityException("Passwords do not match."));

      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn28", "tn28@test.com", "TestPass1!", "Different1!");

      mockMvc
          .perform(
              post("/auth/signup")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }
}
