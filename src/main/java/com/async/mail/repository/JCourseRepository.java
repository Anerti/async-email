package com.async.mail.repository;

import com.async.mail.repository.model.JCourse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JCourseRepository extends JpaRepository<JCourse, UUID> {

  @Query(
      value =
          """
          SELECT id, title, start_date, end_date, price FROM course
          WHERE (:title IS NULL OR :title = '' OR LOWER(title) LIKE LOWER('%' || :title || '%'))
            AND (:startDate IS NULL OR start_date >= CAST(:startDate AS timestamp))
            AND (:endDate IS NULL OR end_date <= CAST(:endDate AS timestamp))
            AND (:price IS NULL OR price = CAST(:price AS numeric))
          ORDER BY title
          OFFSET :offset LIMIT :limit
          """,
      nativeQuery = true)
  List<JCourse> findFiltered(
      @Param("title") String title,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("price") BigDecimal price,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Query(
      value =
          """
          SELECT count(*) FROM course
          WHERE (:title IS NULL OR :title = '' OR LOWER(title) LIKE LOWER('%' || :title || '%'))
            AND (:startDate IS NULL OR start_date >= CAST(:startDate AS timestamp))
            AND (:endDate IS NULL OR end_date <= CAST(:endDate AS timestamp))
            AND (:price IS NULL OR price = CAST(:price AS numeric))
          """,
      nativeQuery = true)
  long countFiltered(
      @Param("title") String title,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("price") BigDecimal price);
}
