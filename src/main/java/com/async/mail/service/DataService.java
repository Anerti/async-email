package com.async.mail.service;

import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.mapper.DataMapper;
import com.async.mail.repository.JDataRepository;
import com.async.mail.repository.model.JData;
import com.async.mail.validator.DataValidator;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class DataService {

  private final JDataRepository dataRepository;
  private final DataMapper dataMapper;
  private final DataValidator dataValidator;

  public DataResponse submitImageData(MultipartFile file, String email) {
    String filename = dataValidator.validateSubmit(file, email);

    JData data = new JData();
    data.setFilename(filename);
    data.setEmail(email);
    data.setCreatedAt(Instant.now());

    return dataMapper.toResponse(dataRepository.save(data));
  }
}
