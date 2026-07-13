package com.async.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.mapper.DataMapper;
import com.async.mail.repository.JDataRepository;
import com.async.mail.repository.model.JData;
import com.async.mail.validator.DataValidator;
import com.async.mail.validator.GeneralValidator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {

  @Mock JDataRepository dataRepository;
  @Mock DataMapper dataMapper;

  @Captor ArgumentCaptor<JData> dataCaptor;

  DataService dataService;

  @BeforeEach
  void setUp() {
    dataService = new DataService(dataRepository, dataMapper, new DataValidator(new GeneralValidator()));
  }

  @Nested
  class Success201 {

    @Test
    void test1_validJpeg_returnsDataResponse() {
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "content".getBytes());
      var saved = new JData();
      saved.setId(UUID.randomUUID());
      saved.setFilename("test.jpeg");
      saved.setEmail("test@example.com");
      saved.setCreatedAt(Instant.now());
      var response = new DataResponse(saved.getId(), saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      var result = dataService.submitImageData(file, "test@example.com");

      assertNotNull(result);
      assertEquals("test.jpeg", result.filename());
      assertEquals("test@example.com", result.email());
    }

    @Test
    void test2_validPng_returnsDataResponse() {
      var file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());
      var saved = new JData();
      saved.setId(UUID.randomUUID());
      saved.setFilename("test.png");
      saved.setEmail("test@example.com");
      saved.setCreatedAt(Instant.now());
      var response = new DataResponse(saved.getId(), saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      var result = dataService.submitImageData(file, "test@example.com");

      assertNotNull(result);
      assertEquals("test.png", result.filename());
    }
  }

  @Nested
  class ValidationErrors422 {

    @Test
    void test3_emailBlank_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "content".getBytes());

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, ""));
      assertTrue(ex.getMessage().contains("email is required"));
    }

    @Test
    void test4_emptyFile_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("file is required and cannot be empty"));
    }

    @Test
    void test5_fileTooLarge_throwsUnprocessable() {
      var oversized = new byte[10 * 1024 * 1024 + 1];
      var file = new MockMultipartFile("file", "big.jpg", "image/jpeg", oversized);

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("file must not exceed 10 MB"));
    }

    @Test
    void test6_unsupportedFormat_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("Unsupported file format"));
    }

    @Test
    void test7_invalidEmail_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "content".getBytes());

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, "not-an-email"));
      assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    void test8_filenameTooLong_throwsUnprocessable() {
      var longName = "f".repeat(101) + ".png";
      var file = new MockMultipartFile("file", longName, "image/png", "content".getBytes());

      var ex = assertThrows(UnprocessableEntityException.class,
          () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("filename must not exceed 100 characters"));
    }
  }
}
