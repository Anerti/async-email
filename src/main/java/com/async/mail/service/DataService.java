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
import java.io.UncheckedIOException;
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

  private static final String EMAIL_TEMPLATE;

  static {
    try {
      EMAIL_TEMPLATE =
          new String(
              new ClassPathResource("email/data-submission.html").getInputStream().readAllBytes(),
              StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final JDataRepository dataRepository;
  private final DataMapper dataMapper;
  private final DataValidator dataValidator;
  private final S3Service s3Service;
  private final EventProducer<SendEmailRequested> eventProducer;

  private static byte[] toGrayscale(byte[] imageBytes) {
    try {
      var original = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (original == null) {
        throw new RuntimeException("Failed to decode image: unsupported format");
      }
      var grayscale =
          new BufferedImage(
              original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      var g = grayscale.createGraphics();
      g.drawImage(original, 0, 0, null);
      g.dispose();
      var baos = new ByteArrayOutputStream();
      ImageIO.write(grayscale, "png", baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to convert image to grayscale", e);
    }
  }

  private static String extension(String filename) {
    return filename != null && filename.contains(".")
        ? filename.substring(filename.lastIndexOf('.') + 1)
        : "png";
  }

  public DataResponse submitImageData(MultipartFile file, String email) {
    dataValidator.validateSubmit(file, email);
    try {
      var id = UUID.randomUUID();
      var now = Instant.now();
      var originalBytes = file.getBytes();
      var ext = extension(file.getOriginalFilename());
      var ts = now.toEpochMilli();

      s3Service.uploadBytes(
          "images/data/%s/original-%d.%s".formatted(id, ts, ext),
          originalBytes,
          file.getContentType());

      var grayscaleBytes = toGrayscale(originalBytes);
      s3Service.uploadBytes(
          "images/data/%s/grayscale-%d.png".formatted(id, ts), grayscaleBytes, "image/png");

      var data = new JData();
      data.setId(id);
      data.setFilename(file.getOriginalFilename());
      data.setEmail(email);
      data.setCreatedAt(now);

      eventProducer.accept(
          List.of(
              SendEmailRequested.builder()
                  .to(email)
                  .subject("Image Processing Complete")
                  .htmlBody(
                      EMAIL_TEMPLATE.formatted(
                          s3Service
                              .generateDownloadUrl(
                                  "images/data/%s/grayscale-%d.png".formatted(id, ts))
                              .toString()))
                  .build()));

      log.info("Image processed: id={}, email={}", id, email);
      return dataMapper.toResponse(dataRepository.save(data));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to process image data", e);
    }
  }
}
