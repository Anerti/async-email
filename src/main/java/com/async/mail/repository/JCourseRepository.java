package com.async.mail.repository;

import com.async.mail.repository.model.JCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JCourseRepository extends JpaRepository<JCourse, UUID> {}
