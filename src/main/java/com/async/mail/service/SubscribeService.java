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

    var attachment = generateInvoiceAttachment(user, course);
    var attachments =
        attachment != null ? List.of(attachment) : List.<SendEmailRequested.Attachment>of();

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
                        course.getEndDate()))
            .attachments(attachments)
            .build();
    eventProducer.accept(List.of(emailEvent));

    return new UserCourseResponse(
        saved.getId(), saved.getUser().getId(), saved.getCourse().getId(), saved.getSubscribedAt());
  }

  private SendEmailRequested.Attachment generateInvoiceAttachment(User user, JCourse course) {
    var invoiceNumber = UUID.randomUUID().toString();
    try {
      var pdfBytes = invoiceService.generateInvoice(user, course, invoiceNumber);
      return SendEmailRequested.Attachment.builder()
          .filename("invoice-" + invoiceNumber + ".pdf")
          .content(pdfBytes)
          .build();
    } catch (Exception e) {
      log.warn(
          "Failed to generate invoice for user {} course {}: {}",
          user.id(),
          course.getId(),
          e.getMessage());
      return null;
    }
  }
}
