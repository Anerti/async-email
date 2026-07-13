package com.async.mail.mapper;

import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.repository.model.JData;
import org.springframework.stereotype.Component;

@Component
public class DataMapper {

  public DataResponse toResponse(JData data) {
    return new DataResponse(data.getId(), data.getFilename(), data.getEmail(), data.getCreatedAt());
  }
}
