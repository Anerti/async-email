package com.async.mail.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.async.mail.endpoint.rest.controller.dto.SignUpRequest;
import com.async.mail.endpoint.rest.controller.dto.UserResponse;
import com.async.mail.entity.enums.UserRole;
import com.async.mail.exception.ConflictException;
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
class AuthServiceSignupTest {

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
  class Success201 {

    @Test
    void test1_valid_all_fields() {
      var request =
          new SignUpRequest(
              "John", "Doe", "john_doe", "john.doe@test.com", "TestPass1!", "TestPass1!");
      var jUser = new JUser();
      var userResponse =
          new UserResponse(
              UUID.randomUUID(), "John", "Doe", "john_doe", "john.doe@test.com", UserRole.CUSTOMER);

      when(passwordEncoder.encode("TestPass1!")).thenReturn("encoded");
      when(authRepository.create(
              "John", "Doe", "john_doe", "john.doe@test.com", "encoded", "CUSTOMER"))
          .thenReturn(Optional.of(jUser));
      when(userMapper.toResponse(jUser)).thenReturn(userResponse);

      var result = authService.signUp(request);

      assertEquals(userResponse, result);
      verify(authRepository)
          .create("John", "Doe", "john_doe", "john.doe@test.com", "encoded", "CUSTOMER");
    }

    @Test
    void test2_valid_accented_names() {
      var request =
          new SignUpRequest(
              "Jéan", "Doe", "jean_doe", "jean.doe@test.com", "TestPass1!", "TestPass1!");
      var jUser = new JUser();
      var userResponse =
          new UserResponse(
              UUID.randomUUID(), "Jéan", "Doe", "jean_doe", "jean.doe@test.com", UserRole.CUSTOMER);

      when(passwordEncoder.encode("TestPass1!")).thenReturn("encoded");
      when(authRepository.create(
              "Jéan", "Doe", "jean_doe", "jean.doe@test.com", "encoded", "CUSTOMER"))
          .thenReturn(Optional.of(jUser));
      when(userMapper.toResponse(jUser)).thenReturn(userResponse);

      var result = authService.signUp(request);

      assertEquals(userResponse, result);
    }
  }

  @Nested
  class Conflict409 {

    @Test
    void test3_duplicate_username() {
      var request =
          new SignUpRequest(
              "Jane", "Smith", "john_doe", "jane.smith@test.com", "TestPass1!", "TestPass1!");

      when(passwordEncoder.encode("TestPass1!")).thenReturn("encoded");
      when(authRepository.create(
              "Jane", "Smith", "john_doe", "jane.smith@test.com", "encoded", "CUSTOMER"))
          .thenReturn(Optional.empty());

      var ex = assertThrows(ConflictException.class, () -> authService.signUp(request));
      assertTrue(
          ex.getMessage().contains("john_doe") || ex.getMessage().contains("jane.smith@test.com"));
    }

    @Test
    void test4_duplicate_email() {
      var request =
          new SignUpRequest(
              "Jane", "Smith", "jane_smith", "john.doe@test.com", "TestPass1!", "TestPass1!");

      when(passwordEncoder.encode("TestPass1!")).thenReturn("encoded");
      when(authRepository.create(
              "Jane", "Smith", "jane_smith", "john.doe@test.com", "encoded", "CUSTOMER"))
          .thenReturn(Optional.empty());

      var ex = assertThrows(ConflictException.class, () -> authService.signUp(request));
      assertTrue(
          ex.getMessage().contains("john.doe@test.com") || ex.getMessage().contains("jane_smith"));
    }
  }

  @Nested
  class ValidationErrors422 {

    // ── lastName ──────────────────────────────────────────────

    @Test
    void test5_lastName_null() {
      var request =
          new SignUpRequest("Marie", null, "tn1", "tn1@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("lastname"));
    }

    @Test
    void test6_lastName_blank() {
      var request =
          new SignUpRequest("Marie", "", "tn2", "tn2@test.com", "TestPass1!", "TestPass1!");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test7_lastName_with_digits() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont123", "tn3", "tn3@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("forbidden"));
    }

    @Test
    void test8_lastName_too_long() {
      var request =
          new SignUpRequest(
              "Marie", "D".repeat(101), "tn4", "tn4@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("longer than 100"));
    }

    // ── firstName ─────────────────────────────────────────────

    @Test
    void test9_firstName_null() {
      var request =
          new SignUpRequest(null, "Dupont", "tn5", "tn5@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("firstname"));
    }

    @Test
    void test10_firstName_blank() {
      var request =
          new SignUpRequest("", "Dupont", "tn6", "tn6@test.com", "TestPass1!", "TestPass1!");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test11_firstName_with_digits() {
      var request =
          new SignUpRequest(
              "Marie123", "Dupont", "tn7", "tn7@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("forbidden"));
    }

    @Test
    void test12_firstName_too_long() {
      var request =
          new SignUpRequest(
              "M".repeat(101), "Dupont", "tn8", "tn8@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("longer than 100"));
    }

    // ── username ──────────────────────────────────────────────

    @Test
    void test13_username_null() {
      var request =
          new SignUpRequest("Marie", "Dupont", null, "tn9@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void test14_username_blank() {
      var request =
          new SignUpRequest("Marie", "Dupont", "", "tn10@test.com", "TestPass1!", "TestPass1!");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test15_username_with_at_sign() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "john@doe", "tn11@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("can only contain letters"));
    }

    @Test
    void test16_username_with_hyphen() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "john-doe", "tn12@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("can only contain letters"));
    }

    @Test
    void test17_username_too_long() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "u".repeat(51), "tn13@test.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("longer than 50"));
    }

    // ── email ─────────────────────────────────────────────────

    @Test
    void test18_email_null() {
      var request = new SignUpRequest("Marie", "Dupont", "tn14", null, "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void test19_email_blank() {
      var request = new SignUpRequest("Marie", "Dupont", "tn15", "", "TestPass1!", "TestPass1!");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test20_email_invalid_format() {
      var request =
          new SignUpRequest("Marie", "Dupont", "tn16", "not-an-email", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("invalid email format"));
    }

    @Test
    void test21_email_forbidden_chars() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn17", "marie @mail.com", "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("invalid input for email"));
    }

    @Test
    void test22_email_too_long() {
      var email = "m".repeat(92) + "@mail.com";
      var request = new SignUpRequest("Marie", "Dupont", "tn18", email, "TestPass1!", "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("longer than 100"));
    }

    // ── password ──────────────────────────────────────────────

    @Test
    void test23_password_null() {
      var request =
          new SignUpRequest("Marie", "Dupont", "tn19", "tn19@test.com", null, "TestPass1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void test24_password_blank() {
      var request = new SignUpRequest("Marie", "Dupont", "tn20", "tn20@test.com", "", "");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test25_password_too_short() {
      var request = new SignUpRequest("Marie", "Dupont", "tn21", "tn21@test.com", "Ab1!", "Ab1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("at least 8"));
    }

    @Test
    void test26_password_no_uppercase() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn22", "tn22@test.com", "lowercase1!", "lowercase1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("uppercase"));
    }

    @Test
    void test27_password_no_lowercase() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn23", "tn23@test.com", "UPPERCASE1!", "UPPERCASE1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("lowercase"));
    }

    @Test
    void test28_password_no_digit() {
      var request =
          new SignUpRequest("Marie", "Dupont", "tn24", "tn24@test.com", "NoDigit!x", "NoDigit!x");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("digit"));
    }

    @Test
    void test29_password_no_special() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn25", "tn25@test.com", "NoSpecial1x", "NoSpecial1x");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("special character"));
    }

    // ── confirmPassword ───────────────────────────────────────

    @Test
    void test30_confirmPassword_null() {
      var request =
          new SignUpRequest("Marie", "Dupont", "tn26", "tn26@test.com", "TestPass1!", null);
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("confirmpassword"));
    }

    @Test
    void test31_confirmPassword_blank() {
      var request = new SignUpRequest("Marie", "Dupont", "tn27", "tn27@test.com", "TestPass1!", "");
      assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
    }

    @Test
    void test32_confirmPassword_mismatch() {
      var request =
          new SignUpRequest(
              "Marie", "Dupont", "tn28", "tn28@test.com", "TestPass1!", "Different1!");
      var ex = assertThrows(UnprocessableEntityException.class, () -> authService.signUp(request));
      assertTrue(ex.getMessage().toLowerCase().contains("passwords do not match"));
    }
  }
}
