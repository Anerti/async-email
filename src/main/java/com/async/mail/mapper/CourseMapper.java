package com.async.mail.mapper;

import com.async.mail.endpoint.rest.controller.dto.CourseResponse;
import com.async.mail.repository.model.JCourse;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

  public CourseResponse toResponse(JCourse course) {
    return new CourseResponse(
        course.getId(),
        course.getTitle(),
        course.getStartDate(),
        course.getEndDate(),
        course.getPrice());
  }
}
