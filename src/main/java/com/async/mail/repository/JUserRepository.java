package com.async.mail.repository;

import com.async.mail.repository.model.JUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JUserRepository extends JpaRepository<JUser, UUID> {}
