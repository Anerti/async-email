package com.async.mail.validator;

import com.async.mail.endpoint.rest.controller.dto.LoginRequest;
import com.async.mail.endpoint.rest.controller.dto.SignUpRequest;
import com.async.mail.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

  private final GeneralValidator generalValidator;

  public void validateSignUp(SignUpRequest request) {
    generalValidator.checkNull("lastName", request.lastName());
    generalValidator.validateName("lastName", request.lastName());

    generalValidator.checkNull("firstName", request.firstName());
    generalValidator.validateName("firstName", request.firstName());

    generalValidator.validateUsername(request.username());

    generalValidator.checkNull("email", request.email());
    generalValidator.validateEmail(request.email());

    generalValidator.checkPasswordSecurityLevel(request.password());

    generalValidator.checkNull("confirmPassword", request.confirmPassword());

    if (!request.password().equals(request.confirmPassword())) {
      throw new UnprocessableEntityException("Passwords do not match.");
    }
  }

  public void validateLogin(LoginRequest request) {
    generalValidator.checkNull("username", request.username());
    generalValidator.validateUsername(request.username());
    generalValidator.checkNull("password", request.password());
  }
}
