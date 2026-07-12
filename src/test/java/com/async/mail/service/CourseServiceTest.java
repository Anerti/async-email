package com.async.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.async.mail.endpoint.rest.controller.dto.CourseResponse;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.mapper.CourseMapper;
import com.async.mail.repository.JCourseRepository;
import com.async.mail.repository.model.JCourse;
import com.async.mail.validator.GeneralValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock JCourseRepository courseRepository;
  @Mock CourseMapper courseMapper;

  CourseService courseService;

  @BeforeEach
  void setUp() {
    courseService = new CourseService(courseRepository, courseMapper, new GeneralValidator());
  }

  @Nested
  class Success200 {

    @Test
    void test1_noFilters_returnsPage1DefaultSize() {
      var now = Instant.now();
      var jCourses =
          List.of(
              jCourse("Mathematics", now, now, BigDecimal.valueOf(199)),
              jCourse("Physics", now, now, BigDecimal.valueOf(249)));
      var responses =
          List.of(
              new CourseResponse(
                  UUID.randomUUID(), "Mathematics", now, now, BigDecimal.valueOf(199)),
              new CourseResponse(UUID.randomUUID(), "Physics", now, now, BigDecimal.valueOf(249)));

      when(courseRepository.findFiltered(null, null, null, null, 0, 10)).thenReturn(jCourses);
      when(courseRepository.countFiltered(null, null, null, null)).thenReturn(18L);
      when(courseMapper.toResponse(jCourses.get(0))).thenReturn(responses.get(0));
      when(courseMapper.toResponse(jCourses.get(1))).thenReturn(responses.get(1));

      var result = courseService.listCourses(null, null, null, null, 1, 10);

      assertEquals(2, result.data().size());
      assertEquals("Mathematics", result.data().get(0).title());
      assertEquals(18, result.meta().total());
      assertEquals(1, result.meta().page());
      assertEquals(10, result.meta().size());
    }

    @Test
    void test2_filterByTitle_returnsMatchingCourses() {
      var now = Instant.now();
      var jCourse = jCourse("Mathematics", now, now, BigDecimal.valueOf(199));
      var response =
          new CourseResponse(UUID.randomUUID(), "Mathematics", now, now, BigDecimal.valueOf(199));

      when(courseRepository.findFiltered("Mathematics", null, null, null, 0, 10))
          .thenReturn(List.of(jCourse));
      when(courseRepository.countFiltered("Mathematics", null, null, null)).thenReturn(1L);
      when(courseMapper.toResponse(jCourse)).thenReturn(response);

      var result = courseService.listCourses("Mathematics", null, null, null, 1, 10);

      assertEquals(1, result.data().size());
      assertEquals("Mathematics", result.data().get(0).title());
      assertEquals(1, result.meta().total());
    }

    @Test
    void test3_filterByPrice_returnsCoursesWithinBudget() {
      var now = Instant.now();
      var jCourse = jCourse("Literature", now, now, BigDecimal.valueOf(149));
      var response =
          new CourseResponse(UUID.randomUUID(), "Literature", now, now, BigDecimal.valueOf(149));

      when(courseRepository.findFiltered(null, null, null, BigDecimal.valueOf(149), 0, 10))
          .thenReturn(List.of(jCourse));
      when(courseRepository.countFiltered(null, null, null, BigDecimal.valueOf(149)))
          .thenReturn(1L);
      when(courseMapper.toResponse(jCourse)).thenReturn(response);

      var result = courseService.listCourses(null, null, null, BigDecimal.valueOf(149), 1, 10);

      assertEquals("Literature", result.data().get(0).title());
      assertEquals(1, result.meta().total());
    }

    @Test
    void test4_blankTitle_noFilterApplied() {
      var now = Instant.now();
      var jCourse = jCourse("Math", now, now, BigDecimal.valueOf(100));
      var response =
          new CourseResponse(UUID.randomUUID(), "Math", now, now, BigDecimal.valueOf(100));

      when(courseRepository.findFiltered("", null, null, null, 0, 10)).thenReturn(List.of(jCourse));
      when(courseRepository.countFiltered("", null, null, null)).thenReturn(1L);
      when(courseMapper.toResponse(jCourse)).thenReturn(response);

      var result = courseService.listCourses("", null, null, null, 1, 10);

      assertEquals(1, result.data().size());
      assertEquals(1, result.meta().total());
    }

    @Test
    void test5_nonexistentTitle_emptyResults() {
      when(courseRepository.findFiltered("Nonexistent", null, null, null, 0, 10))
          .thenReturn(List.of());
      when(courseRepository.countFiltered("Nonexistent", null, null, null)).thenReturn(0L);

      var result = courseService.listCourses("Nonexistent", null, null, null, 1, 10);

      assertNull(result.data());
      assertEquals(0, result.meta().total());
    }

    @Test
    void test6_pagination_correctOffset() {
      var now = Instant.now();
      var jCourses =
          List.of(
              jCourse("A", now, now, BigDecimal.valueOf(10)),
              jCourse("B", now, now, BigDecimal.valueOf(20)));
      var responses =
          List.of(
              new CourseResponse(UUID.randomUUID(), "A", now, now, BigDecimal.valueOf(10)),
              new CourseResponse(UUID.randomUUID(), "B", now, now, BigDecimal.valueOf(20)));

      when(courseRepository.findFiltered(null, null, null, null, 5, 5)).thenReturn(jCourses);
      when(courseRepository.countFiltered(null, null, null, null)).thenReturn(18L);
      when(courseMapper.toResponse(jCourses.get(0))).thenReturn(responses.get(0));
      when(courseMapper.toResponse(jCourses.get(1))).thenReturn(responses.get(1));

      var result = courseService.listCourses(null, null, null, null, 2, 5);

      assertEquals(2, result.data().size());
      assertEquals(2, result.meta().page());
      assertEquals(5, result.meta().size());
    }

    @Test
    void test7_oversizedPage_emptyResults() {
      when(courseRepository.findFiltered(null, null, null, null, 20, 10)).thenReturn(List.of());
      when(courseRepository.countFiltered(null, null, null, null)).thenReturn(18L);

      var result = courseService.listCourses(null, null, null, null, 3, 10);

      assertNull(result.data());
      assertEquals(18, result.meta().total());
    }
  }

  @Nested
  class ValidationErrors422 {

    @Test
    void test8_invalidTitleSpecialChar_throwsUnprocessable() {
      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> courseService.listCourses("@invalid", null, null, null, 1, 10));
      assertTrue(ex.getMessage().toLowerCase().contains("forbidden"));
    }

    @Test
    void test9_titleTooLong_throwsUnprocessable() {
      var ex =
          assertThrows(
              UnprocessableEntityException.class,
              () -> courseService.listCourses("A".repeat(101), null, null, null, 1, 10));
      assertTrue(ex.getMessage().toLowerCase().contains("longer than 100"));
    }
  }

  private static JCourse jCourse(String title, Instant start, Instant end, BigDecimal price) {
    var jc = new JCourse();
    jc.setTitle(title);
    jc.setStartDate(start);
    jc.setEndDate(end);
    jc.setPrice(price);
    return jc;
  }
}
