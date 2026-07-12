package com.async.mail.service;

import com.async.mail.config.ResourcesAccessRules;
import com.async.mail.endpoint.event.EventProducer;
import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.endpoint.rest.controller.dto.UserCourseResponse;
import com.async.mail.entity.User;
import com.async.mail.exception.ConflictException;
import com.async.mail.exception.ForbiddenException;
import com.async.mail.exception.NotFoundException;
import com.async.mail.repository.JCourseRepository;
import com.async.mail.repository.JUserCourseRepository;
import com.async.mail.repository.JUserRepository;
import com.async.mail.repository.model.JCourse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class SubscribeService {

  private final JUserRepository userRepository;
  private final JCourseRepository courseRepository;
  private final JUserCourseRepository userCourseRepository;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final ResourcesAccessRules resourcesAccessRules;
  private final InvoiceService invoiceService;
  private final S3Service s3Service;
  private final QrCodeService qrCodeService;

  private static String emailTemplate() {
    try {
      return new String(
          new ClassPathResource("email/subscription-confirmation.html")
              .getInputStream()
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load email template", e);
    }
  }

  @Transactional
  public UserCourseResponse subscribe(UUID userId, UUID courseId) {
    var jUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(String.format("User %s not found", userId)));
    var user =
        new User(
            jUser.getId(),
            jUser.getFirstName(),
            jUser.getLastName(),
            jUser.getUsername(),
            jUser.getEmail(),
            jUser.getRole());

    if (!resourcesAccessRules.grantAccessFor(user)) {
      throw new ForbiddenException(
          String.format("Cannot subscribe user %s to course %s", userId, courseId));
    }

    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(
                () -> new NotFoundException(String.format("Course %s not found", courseId)));

    var saved =
        userCourseRepository
            .insertOnConflictReturning(userId, courseId, Instant.now())
            .orElseThrow(
                () ->
                    new ConflictException(
                        String.format("You are already subscribed in course %s", courseId)));

    var qrDataUri = generateInvoiceQrDataUri(user, course, userId, courseId);

    var emailEvent =
        SendEmailRequested.builder()
            .to(user.email())
            .subject(String.format("Subscription to %s", course.getTitle()))
            .htmlBody(
                emailTemplate()
                    .formatted(
                        user.firstName(),
                        user.lastName(),
                        course.getTitle(),
                        course.getTitle(),
                        course.getStartDate(),
                        course.getEndDate(),
                        qrDataUri))
            .build();
    eventProducer.accept(List.of(emailEvent));

    return new UserCourseResponse(
        saved.getId(), saved.getUser().getId(), saved.getCourse().getId(), saved.getSubscribedAt());
  }

  private String generateInvoiceQrDataUri(User user, JCourse course, UUID userId, UUID courseId) {
    var invoiceNumber = UUID.randomUUID().toString();
    try {
      var pdfBytes = invoiceService.generateInvoice(user, course, invoiceNumber);
      var s3Key = s3Service.uploadInvoice(userId, courseId, pdfBytes);
      var downloadUrl = s3Service.generateDownloadUrl(s3Key);
      return qrCodeService.generateQrDataUri(downloadUrl.toString());
    } catch (Exception e) {
      log.warn(
          "Failed to generate or upload invoice for user {} course {}: {}",
          userId,
          courseId,
          e.getMessage());
      return "";
    }
  }
}
