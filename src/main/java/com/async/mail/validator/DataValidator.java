package com.async.mail.validator;

import com.async.mail.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class DataValidator {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  private final GeneralValidator generalValidator;

  public void validateSubmit(MultipartFile file, String email) {
    if (email == null || email.isBlank()) {
      throw new UnprocessableEntityException("email is required.");
    }
    generalValidator.validateEmail(email);

    if (file == null || file.isEmpty()) {
      throw new UnprocessableEntityException("file is required and cannot be empty.");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new UnprocessableEntityException("file must not exceed 10 MB.");
    }

    String contentType = file.getContentType();
    if (contentType == null
        || !(contentType.equals("image/jpeg")
            || contentType.equals("image/png")
            || contentType.equals("image/jpg"))) {
      throw new UnprocessableEntityException(
          "Unsupported file format. Only JPEG and PNG images are allowed.");
    }

    String filename = file.getOriginalFilename();
    if (filename == null || filename.isBlank()) {
      throw new UnprocessableEntityException("file must have a valid filename.");
    }

    if (filename.length() > 100) {
      throw new UnprocessableEntityException("filename must not exceed 100 characters.");
    }
  }
}
