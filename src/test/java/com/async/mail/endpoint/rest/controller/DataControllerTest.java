package com.async.mail.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.exception.GlobalExceptionHandler;
import com.async.mail.service.DataService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

  @Mock DataService dataService;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new DataController(dataService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Nested
  class Success201 {

    @Test
    void test1_validJpeg_returns201() throws Exception {
      var now = Instant.now();
      var id = UUID.randomUUID();
      var response = new DataResponse(id, "test.jpeg", "test@example.com", now);

      when(dataService.submitImageData(any(), eq("test@example.com"))).thenReturn(response);

      var file =
          new MockMultipartFile("file", "test.jpeg", "image/jpeg", "fake-image-content".getBytes());
      var emailPart =
          new MockMultipartFile("email", "", "text/plain", "test@example.com".getBytes());

      mockMvc
          .perform(multipart("/data").file(file).file(emailPart))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.filename").value("test.jpeg"))
          .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void test2_validPng_returns201() throws Exception {
      var now = Instant.now();
      var id = UUID.randomUUID();
      var response = new DataResponse(id, "test.png", "test@example.com", now);

      when(dataService.submitImageData(any(), eq("test@example.com"))).thenReturn(response);

      var file =
          new MockMultipartFile("file", "test.png", "image/png", "fake-png-content".getBytes());
      var emailPart =
          new MockMultipartFile("email", "", "text/plain", "test@example.com".getBytes());

      mockMvc
          .perform(multipart("/data").file(file).file(emailPart))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.filename").value("test.png"))
          .andExpect(jsonPath("$.email").value("test@example.com"));
    }
  }
}
