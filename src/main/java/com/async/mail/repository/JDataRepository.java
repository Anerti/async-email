package com.async.mail.repository;

import com.async.mail.repository.model.JData;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JDataRepository extends JpaRepository<JData, UUID> {}
