package com.async.mail.validator;

import com.async.mail.exception.UnprocessableEntityException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GeneralValidator {

  private static final Pattern SAFE_STRING = Pattern.compile("^[a-zA-Z0-9@'éèê ._+\\-]*$");
  private static final Pattern SAFE_NAME_STRING = Pattern.compile("^[a-zA-Zéèê' -]+$");
  private static final Pattern VALID_EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z]+){1,2}$");
  private static final Pattern ALLOWED_EMAIL_CHAR = Pattern.compile("^[a-zA-Z0-9.@_-]+$");
  private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

  public void checkNull(String fieldName, Object value) {
    if (value == null || value.toString().isBlank()) {
      throw new UnprocessableEntityException(
          String.format("%s is required and cannot be blank.", fieldName));
    }
  }

  public void checkStringLength(String fieldName, String value, int length) {
    if (value != null && value.length() > length) {
      throw new UnprocessableEntityException(
          String.format("%s cannot be longer than %s characters.", fieldName, length));
    }
  }

  public void validateString(String fieldName, String value) {
    if (value != null && !value.isBlank() && !SAFE_STRING.matcher(value).matches()) {
      throw new UnprocessableEntityException(
          String.format(
              "Field '%s' contains invalid characters. Only letters (a-z, A-Z), digits (0-9),"
                  + " spaces, and @ ('.-_) are allowed.",
              fieldName));
    }
  }

  public void validateEmail(String email) {
    if (email != null && !email.isBlank()) {
      checkStringLength("email", email, 100);

      if (!ALLOWED_EMAIL_CHAR.matcher(email).matches()) {
        throw new UnprocessableEntityException(
            String.format(
                "Invalid input for email: '%s' only a-zA-Z0-9@_.- characters are allowed.",
                email));
      }

      if (!VALID_EMAIL_PATTERN.matcher(email).matches()) {
        throw new UnprocessableEntityException(
            String.format("Invalid email format: '%s'", email));
      }
    }
  }

  public void checkPasswordSecurityLevel(String password) {
    checkNull("password", password);

    if (password.length() < 8) {
      throw new UnprocessableEntityException("password must be at least 8 characters.");
    }

    if (password.length() > 128) {
      throw new UnprocessableEntityException("password must not exceed 128 characters.");
    }

    if (!password.matches(".*[A-Z].*")) {
      throw new UnprocessableEntityException(
          "Password must contain at least one uppercase character.");
    }

    if (!password.matches(".*[a-z].*")) {
      throw new UnprocessableEntityException(
          "Password must contain at least one lowercase character.");
    }

    if (!password.matches(".*[0-9].*")) {
      throw new UnprocessableEntityException("Password must contain at least one digit.");
    }

    if (!password.matches(".*[!?*+=@#$%^&()_\\-\\[\\]{}|\\\\:;\"'<>,./`~].*")) {
      throw new UnprocessableEntityException(
          "Password must contain at least one special character.");
    }
  }

  public void validateName(String fieldName, String value) {
    if (value != null && !value.isBlank()) {
      checkStringLength(fieldName, value, 100);

      if (!SAFE_NAME_STRING.matcher(value).matches()) {
        throw new UnprocessableEntityException(
            String.format(
                "%s field contain forbidden characters. "
                    + "Only letters (a-z, A-Z, éèê), hyphen and space are allowed.",
                fieldName));
      }
    }
  }

  public void validateUsername(String username) {
    checkNull("username", username);
    checkStringLength("username", username, 50);

    if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
      throw new UnprocessableEntityException(
          "Username can only contain letters (a-z, A-Z), digits (0-9) and underscores.");
    }
  }
}
