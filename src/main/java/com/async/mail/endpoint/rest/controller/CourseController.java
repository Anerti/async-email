package com.async.mail.endpoint.rest.controller;

import com.async.mail.endpoint.rest.controller.dto.CourseListResponse;
import com.async.mail.service.CourseService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @GetMapping("/courses")
  public ResponseEntity<CourseListResponse> listCourses(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant endDate,
      @RequestParam(required = false) BigDecimal price,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize) {
    return ResponseEntity.status(HttpStatus.OK).body(courseService.listCourses(title, startDate, endDate, price, page, pageSize));
  }
}
