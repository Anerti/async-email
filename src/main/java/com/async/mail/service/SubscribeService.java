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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SubscribeService {

  private final JUserRepository userRepository;
  private final JCourseRepository courseRepository;
  private final JUserCourseRepository userCourseRepository;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final ResourcesAccessRules resourcesAccessRules;

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
      throw new ForbiddenException(String.format("Cannot subscribe user %s to course %s", userId, courseId));
    }
    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException(String.format("Course %s not found", courseId)));

    var saved =
        userCourseRepository
            .insertOnConflictReturning(userId, courseId, Instant.now())
            .orElseThrow(
                () ->
                    new ConflictException(
                        String.format("You are already subscribed in course %s", courseId)));

    var emailEvent =
        SendEmailRequested.builder()
            .to(user.email())
            .subject("Subscription to " + course.getTitle())
            .htmlBody(
                """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"></head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background-color:#f4f4f4">
                  <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;max-width:560px;margin:40px auto;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08)">
                    <tr>
                      <td style="padding:32px 32px 16px;background:linear-gradient(135deg,#2563eb,#1d4ed8);text-align:center">
                        <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:600">Subscription Confirmed</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:24px 32px;color:#334155;font-size:15px;line-height:1.6">
                        <p style="margin:0 0 16px">Hello %s %s,</p>
                        <p style="margin:0 0 16px">You have successfully subscribed to <strong style="color:#2563eb">%s</strong>.</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;margin:20px 0;background-color:#f8fafc;border-radius:6px;border:1px solid #e2e8f0">
                          <tr>
                            <td style="padding:16px 20px">
                              <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%">
                                <tr>
                                  <td style="color:#64748b;font-size:13px;padding-bottom:4px">Course</td>
                                  <td style="text-align:right;color:#0f172a;font-size:14px;font-weight:600;padding-bottom:4px">%s</td>
                                </tr>
                                <tr>
                                  <td style="color:#64748b;font-size:13px;padding-bottom:4px">Start date</td>
                                  <td style="text-align:right;color:#0f172a;font-size:14px;padding-bottom:4px">%s</td>
                                </tr>
                                <tr>
                                  <td style="color:#64748b;font-size:13px">End date</td>
                                  <td style="text-align:right;color:#0f172a;font-size:14px">%s</td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:0 0 4px;color:#64748b;font-size:13px">If you have any questions, feel free to reach out to our support team.</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:16px 32px;background-color:#f8fafc;border-top:1px solid #e2e8f0;text-align:center;color:#94a3b8;font-size:12px">
                        &copy; Async Mail &mdash; All rights reserved.
                      </td>
                    </tr>
                  </table>
                </body>
                </html>"""
                .formatted(
                    user.firstName(),
                    user.lastName(),
                    course.getTitle(),
                    course.getTitle(),
                    course.getStartDate(),
                    course.getEndDate()))
            .build();
    eventProducer.accept(List.of(emailEvent));

    return new UserCourseResponse(
        saved.getId(), saved.getUser().getId(), saved.getCourse().getId(), saved.getSubscribedAt());
  }
}
