package com.async.mail.service;

import com.async.mail.endpoint.event.EventProducer;
import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.mapper.DataMapper;
import com.async.mail.repository.JDataRepository;
import com.async.mail.repository.model.JData;
import com.async.mail.validator.DataValidator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
public class DataService {

  private final JDataRepository dataRepository;
  private final DataMapper dataMapper;
  private final DataValidator dataValidator;
  private final S3Service s3Service;
  private final EventProducer<SendEmailRequested> eventProducer;

  private static String emailTemplate() {
    try {
      return new String(
          new ClassPathResource("email/data-submission.html")
              .getInputStream()
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load email template", e);
    }
  }

  private static String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "png";
    }
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  private static byte[] convertToGrayscale(byte[] imageBytes) {
    try {
      var original = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (original == null) {
        throw new RuntimeException("Failed to decode image: unsupported format");
      }

      var grayscale =
          new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      var g = grayscale.createGraphics();
      g.drawImage(original, 0, 0, null);
      g.dispose();

      var baos = new ByteArrayOutputStream();
      ImageIO.write(grayscale, "png", baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert image to grayscale", e);
    }
  }

  public DataResponse submitImageData(MultipartFile file, String email) {
    dataValidator.validateSubmit(file, email);

    try {
      var originalBytes = file.getBytes();
      var id = UUID.randomUUID();
      var now = Instant.now();
      var ext = getExtension(file.getOriginalFilename());

      var originalKey =
          String.format(
              "images/data/%s/original-%d.%s", id, now.toEpochMilli(), ext);
      s3Service.uploadBytes(originalKey, originalBytes, file.getContentType());

      var grayscaleBytes = convertToGrayscale(originalBytes);
      var grayscaleKey =
          String.format("images/data/%s/grayscale-%d.png", id, now.toEpochMilli());
      s3Service.uploadBytes(grayscaleKey, grayscaleBytes, "image/png");

      var grayscaleUrl = s3Service.generateDownloadUrl(grayscaleKey).toString();

      var data = new JData();
      data.setId(id);
      data.setFilename(file.getOriginalFilename());
      data.setEmail(email);
      data.setCreatedAt(now);
      var saved = dataRepository.save(data);

      var emailEvent =
          SendEmailRequested.builder()
              .to(email)
              .subject("Your Image Processing Result")
              .htmlBody(emailTemplate().formatted(grayscaleUrl))
              .build();
      eventProducer.accept(List.of(emailEvent));

      log.info("Image processed: id={}, email={}, grayscaleUrl={}", id, email, grayscaleUrl);

      return dataMapper.toResponse(saved);
    } catch (IOException e) {
      throw new RuntimeException("Failed to process image data", e);
    }
  }
}
