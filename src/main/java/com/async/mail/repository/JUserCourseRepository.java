package com.async.mail.repository;

import com.async.mail.repository.model.JUserCourse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JUserCourseRepository extends JpaRepository<JUserCourse, UUID> {

  @Query(
      value =
          """
                INSERT INTO user_course (id, user_id, course_id, subscribed_at)
                VALUES (gen_random_uuid(), :userId, :courseId, :subscribedAt)
                ON CONFLICT (user_id, course_id) DO NOTHING RETURNING *
          """,
      nativeQuery = true)
  Optional<JUserCourse> insertOnConflictReturning(
      @Param("userId") UUID userId,
      @Param("courseId") UUID courseId,
      @Param("subscribedAt") Instant subscribedAt);
}
