package com.async.mail.endpoint.rest.controller;

import com.async.mail.endpoint.rest.controller.dto.UserCourseResponse;
import com.async.mail.service.SubscribeService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SubscribeController {

  private final SubscribeService subscribeService;

  @PostMapping("/users/{userId}/courses/{courseId}")
  public ResponseEntity<UserCourseResponse> subscribeToCourse(
      @PathVariable UUID userId, @PathVariable UUID courseId) {
    var response = subscribeService.subscribe(userId, courseId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
