package com.async.mail.repository;

import com.async.mail.repository.model.JData;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JDataRepository extends JpaRepository<JData, UUID> {

  @Query(
      value =
          """
SELECT id, filename, email, created_at FROM data
WHERE (:email IS NULL OR :email = '' OR email = :email)
  AND (:filename IS NULL OR :filename = '' OR LOWER(filename) LIKE LOWER('%' || :filename || '%'))
ORDER BY created_at DESC
OFFSET :offset LIMIT :limit
""",
      nativeQuery = true)
  List<JData> findFiltered(
      @Param("email") String email,
      @Param("filename") String filename,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Query(
      value =
          """
SELECT count(id) FROM data
WHERE (:email IS NULL OR :email = '' OR email = :email)
  AND (:filename IS NULL OR :filename = '' OR LOWER(filename) LIKE LOWER('%' || :filename || '%'))
""",
      nativeQuery = true)
  long countFiltered(@Param("email") String email, @Param("filename") String filename);
}
