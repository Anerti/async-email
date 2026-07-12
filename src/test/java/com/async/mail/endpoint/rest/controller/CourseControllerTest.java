package com.async.mail.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.async.mail.endpoint.rest.controller.dto.CourseListResponse;
import com.async.mail.endpoint.rest.controller.dto.CourseResponse;
import com.async.mail.endpoint.rest.controller.dto.Meta;
import com.async.mail.exception.GlobalExceptionHandler;
import com.async.mail.exception.UnprocessableEntityException;
import com.async.mail.service.CourseService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

  @Mock CourseService courseService;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CourseController(courseService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Nested
  class Success200 {

    @Test
    void test1_noFilters_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var courses =
          List.of(
              new CourseResponse(
                  UUID.fromString("abfbbc98-bab0-4d91-8c0f-974c20be5d95"),
                  "Mathematics",
                  now,
                  now,
                  BigDecimal.valueOf(199.00)),
              new CourseResponse(
                  UUID.fromString("2db4043c-540f-4b06-8307-cd6b010a293f"),
                  "Computer Science",
                  now,
                  now,
                  BigDecimal.valueOf(299.00)));
      var response = new CourseListResponse(courses, new Meta(1, 10, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].title").value("Mathematics"))
          .andExpect(jsonPath("$.meta.page").value(1))
          .andExpect(jsonPath("$.meta.size").value(10))
          .andExpect(jsonPath("$.meta.total").value(18));
    }

    @Test
    void test2_filterByTitle_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.fromString("abfbbc98-bab0-4d91-8c0f-974c20be5d95"),
              "Mathematics",
              now,
              now,
              BigDecimal.valueOf(199.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 1));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("title", "Mathematics"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("Mathematics"))
          .andExpect(jsonPath("$.meta.total").value(1));
    }

    @Test
    void test3_filterByPrice_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.fromString("93b47401-9dfe-4463-acba-43b2817c25bd"),
              "Literature",
              now,
              now,
              BigDecimal.valueOf(149.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 1));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("price", "149.00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("Literature"))
          .andExpect(jsonPath("$.data[0].price").value(149.00))
          .andExpect(jsonPath("$.meta.total").value(1));
    }

    @Test
    void test4_filterByStartDate_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.randomUUID(), "Mathematics", now, now, BigDecimal.valueOf(199.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("startDate", "2026-09-01T09:00:00Z"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("Mathematics"))
          .andExpect(jsonPath("$.meta.total").value(18));
    }

    @Test
    void test5_filterByEndDate_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.randomUUID(), "Mathematics", now, now, BigDecimal.valueOf(199.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("endDate", "2027-01-15T17:00:00Z"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.meta.total").value(18));
    }

    @Test
    void test6_combinedTitleAndPrice_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.fromString("03efbf3d-7a38-4d0a-81e1-4d129f24451a"),
              "Physics",
              now,
              now,
              BigDecimal.valueOf(249.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 1));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("title", "Physics").param("price", "249.00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("Physics"))
          .andExpect(jsonPath("$.meta.total").value(1));
    }

    @Test
    void test7_pagination_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var courses =
          List.of(
              new CourseResponse(UUID.randomUUID(), "A", now, now, BigDecimal.valueOf(10)),
              new CourseResponse(UUID.randomUUID(), "B", now, now, BigDecimal.valueOf(20)));
      var response = new CourseListResponse(courses, new Meta(1, 5, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("page", "1").param("pageSize", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.meta.page").value(1))
          .andExpect(jsonPath("$.meta.size").value(5));
    }

    @Test
    void test8_blankTitle_returns200() throws Exception {
      var now = Instant.parse("2026-09-01T09:00:00Z");
      var course =
          new CourseResponse(
              UUID.randomUUID(), "Mathematics", now, now, BigDecimal.valueOf(199.00));
      var response = new CourseListResponse(List.of(course), new Meta(1, 10, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("title", ""))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("Mathematics"));
    }

    @Test
    void test9_nonexistentTitle_emptyData() throws Exception {
      var response = new CourseListResponse(null, new Meta(1, 10, 0));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("title", "Nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").doesNotExist())
          .andExpect(jsonPath("$.meta.total").value(0));
    }

    @Test
    void test10_oversizedPage_emptyData() throws Exception {
      var response = new CourseListResponse(null, new Meta(3, 10, 18));

      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenReturn(response);

      mockMvc
          .perform(get("/courses").param("page", "3").param("pageSize", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").doesNotExist())
          .andExpect(jsonPath("$.meta.page").value(3))
          .andExpect(jsonPath("$.meta.size").value(10))
          .andExpect(jsonPath("$.meta.total").value(18));
    }
  }

  @Nested
  class ValidationErrors422 {

    @Test
    void test11_titleWithForbiddenChar_returns422() throws Exception {
      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenThrow(
              new UnprocessableEntityException(
                  "title field contain forbidden characters. "
                      + "Only letters (a-z, A-Z, éèê), hyphen and space are allowed."));

      mockMvc
          .perform(get("/courses").param("title", "@invalid"))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void test12_titleTooLong_returns422() throws Exception {
      when(courseService.listCourses(any(), any(), any(), any(), anyInt(), anyInt()))
          .thenThrow(
              new UnprocessableEntityException("title cannot be longer than 100 characters."));

      mockMvc
          .perform(get("/courses").param("title", "A".repeat(101)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.status").value(422))
          .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }
  }

  @Nested
  class BadRequest400 {

    @Test
    void test13_invalidPage_returns400() throws Exception {
      mockMvc
          .perform(get("/courses").param("page", "abc"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("Invalid parameter: page"));
    }

    @Test
    void test14_invalidPrice_returns400() throws Exception {
      mockMvc
          .perform(get("/courses").param("price", "abc"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("Invalid parameter: price"));
    }

    @Test
    void test15_invalidStartDate_returns400() throws Exception {
      mockMvc
          .perform(get("/courses").param("startDate", "not-a-date"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
  }
}
