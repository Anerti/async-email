package com.async.mail.repository;

import com.async.mail.repository.model.JUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<JUser, UUID> {

  @Query(
      value =
          """
INSERT INTO "user" (id, first_name, last_name, username, email, password, role)
SELECT gen_random_uuid(), :firstName, :lastName, :username, :email, :password, CAST(:role AS user_role)
WHERE NOT EXISTS (
    SELECT 1 FROM "user" WHERE username = :username OR email = :email
)
RETURNING id, first_name, last_name, username, email, password, role
""",
      nativeQuery = true)
  Optional<JUser> create(
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("username") String username,
      @Param("email") String email,
      @Param("password") String password,
      @Param("role") String role);
}
