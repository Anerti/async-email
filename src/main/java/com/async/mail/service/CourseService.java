package com.async.mail.service;

import com.async.mail.endpoint.rest.controller.dto.CourseListResponse;
import com.async.mail.endpoint.rest.controller.dto.CourseResponse;
import com.async.mail.endpoint.rest.controller.dto.Meta;
import com.async.mail.mapper.CourseMapper;
import com.async.mail.repository.JCourseRepository;
import com.async.mail.validator.GeneralValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

  private final JCourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final GeneralValidator generalValidator;

  public CourseListResponse listCourses(
      String title, Instant startDate, Instant endDate, BigDecimal price, int page, int size) {

    generalValidator.validateName("title", title);

    int offset = (page - 1) * size;

    List<CourseResponse> data =
        courseRepository.findFiltered(title, startDate, endDate, price, offset, size).stream()
            .map(courseMapper::toResponse)
            .toList();

    long total = courseRepository.countFiltered(title, startDate, endDate, price);

    return new CourseListResponse(data.isEmpty() ? null : data, new Meta(page, size, total));
  }
}
