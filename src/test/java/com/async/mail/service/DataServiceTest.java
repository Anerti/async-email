package com.async.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.async.mail.endpoint.event.EventProducer;
import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.mapper.DataMapper;
import com.async.mail.repository.JDataRepository;
import com.async.mail.repository.model.JData;
import com.async.mail.validator.DataValidator;
import com.async.mail.validator.GeneralValidator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import javax.imageio.ImageIO;
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
  @Mock S3Service s3Service;
  @Mock EventProducer<SendEmailRequested> eventProducer;

  @Captor @SuppressWarnings("rawtypes") ArgumentCaptor<Collection> eventCaptor;
  @Captor ArgumentCaptor<String> uploadCaptor;

  DataService dataService;

  private static final URL MOCK_PRESIGNED_URL;

  static {
    try {
      MOCK_PRESIGNED_URL = URI.create("https://s3.example.com/data/grayscale.png").toURL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static byte[] createTestImageBytes() {
    try {
      var img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
      var baos = new ByteArrayOutputStream();
      ImageIO.write(img, "png", baos);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setUp() {
    dataService =
        new DataService(
            dataRepository, dataMapper, new DataValidator(new GeneralValidator()), s3Service, eventProducer);
  }

  @Nested
  class SuccessCases {

    @Test
    void test1_validJpeg_returnsDataResponse() {
      var imageBytes = createTestImageBytes();
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", imageBytes);
      var saved = new JData();
      saved.setId(UUID.randomUUID());
      saved.setFilename("test.jpeg");
      saved.setEmail("test@example.com");
      saved.setCreatedAt(Instant.now());
      var response =
          new DataResponse(
              saved.getId(), saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(s3Service.generateDownloadUrl(any())).thenReturn(MOCK_PRESIGNED_URL);
      doNothing().when(eventProducer).accept(any());
      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      var result = dataService.submitImageData(file, "test@example.com");

      assertNotNull(result);
      assertEquals("test.jpeg", result.filename());
      assertEquals("test@example.com", result.email());
    }

    @Test
    void test2_validPng_returnsDataResponse() {
      var imageBytes = createTestImageBytes();
      var file = new MockMultipartFile("file", "test.png", "image/png", imageBytes);
      var saved = new JData();
      saved.setId(UUID.randomUUID());
      saved.setFilename("test.png");
      saved.setEmail("test@example.com");
      saved.setCreatedAt(Instant.now());
      var response =
          new DataResponse(
              saved.getId(), saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(s3Service.generateDownloadUrl(any())).thenReturn(MOCK_PRESIGNED_URL);
      doNothing().when(eventProducer).accept(any());
      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      var result = dataService.submitImageData(file, "test@example.com");

      assertNotNull(result);
      assertEquals("test.png", result.filename());
    }

    @Test
    void test9_sendsAsyncEmailWithPresignedUrl() throws Exception {
      var imageBytes = createTestImageBytes();
      var file = new MockMultipartFile("file", "photo.jpeg", "image/jpeg", imageBytes);
      var saved = new JData();
      var id = UUID.randomUUID();
      var now = Instant.now();
      saved.setId(id);
      saved.setFilename("photo.jpeg");
      saved.setEmail("user@example.com");
      saved.setCreatedAt(now);
      var response =
          new DataResponse(id, saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(s3Service.generateDownloadUrl(any())).thenReturn(MOCK_PRESIGNED_URL);
      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      dataService.submitImageData(file, "user@example.com");

      verify(s3Service, times(2)).uploadBytes(any(), any(), any());
      verify(s3Service).generateDownloadUrl(any());
      verify(eventProducer).accept(eventCaptor.capture());

      var events = eventCaptor.getValue();
      assertEquals(1, events.size());

      var event = (SendEmailRequested) events.iterator().next();
      assertEquals("user@example.com", event.getTo());
      assertEquals("Your Image Processing Result", event.getSubject());
      assertTrue(event.getHtmlBody().contains(MOCK_PRESIGNED_URL.toString()));
      assertNull(event.getAttachments());
    }

    @Test
    void test10_uploadsOriginalAndGrayscaleToS3() {
      var imageBytes = createTestImageBytes();
      var file = new MockMultipartFile("file", "photo.jpeg", "image/jpeg", imageBytes);
      var saved = new JData();
      saved.setId(UUID.randomUUID());
      saved.setFilename("photo.jpeg");
      saved.setEmail("user@example.com");
      saved.setCreatedAt(Instant.now());
      var response =
          new DataResponse(
              saved.getId(), saved.getFilename(), saved.getEmail(), saved.getCreatedAt());

      when(s3Service.generateDownloadUrl(any())).thenReturn(MOCK_PRESIGNED_URL);
      doNothing().when(eventProducer).accept(any());
      when(dataRepository.save(any())).thenReturn(saved);
      when(dataMapper.toResponse(saved)).thenReturn(response);

      dataService.submitImageData(file, "user@example.com");

      verify(s3Service, times(2))
          .uploadBytes(uploadCaptor.capture(), any(), any());

      var keys = uploadCaptor.getAllValues();
      assertEquals(2, keys.size());
      assertTrue(keys.get(0).contains("original"));
      assertTrue(keys.get(1).contains("grayscale"));
    }
  }

  @Nested
  class ValidationErrors422 {

    @Test
    void test3_emailBlank_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", createTestImageBytes());

      var ex =
          assertThrows(
              UnprocessableEntityException.class, () -> dataService.submitImageData(file, ""));
      assertTrue(ex.getMessage().contains("email is required"));
    }

    @Test
    void test4_emptyFile_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("file is required and cannot be empty"));
    }

    @Test
    void test5_fileTooLarge_throwsUnprocessable() {
      var oversized = new byte[10 * 1024 * 1024 + 1];
      var file = new MockMultipartFile("file", "big.jpg", "image/jpeg", oversized);

      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("file must not exceed 10 MB"));
    }

    @Test
    void test6_unsupportedFormat_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("Unsupported file format"));
    }

    @Test
    void test7_invalidEmail_throwsUnprocessable() {
      var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", createTestImageBytes());

      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> dataService.submitImageData(file, "not-an-email"));
      assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    void test8_filenameTooLong_throwsUnprocessable() {
      var longName = "f".repeat(101) + ".png";
      var file = new MockMultipartFile("file", longName, "image/png", createTestImageBytes());

      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> dataService.submitImageData(file, "test@example.com"));
      assertTrue(ex.getMessage().contains("filename must not exceed 100 characters"));
    }
  }
}
