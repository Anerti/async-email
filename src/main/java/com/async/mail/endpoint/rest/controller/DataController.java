package com.async.mail.endpoint.rest.controller;

import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.service.DataService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class DataController {

  private final DataService dataService;

  @PostMapping("/data")
  public ResponseEntity<DataResponse> submitImageData(
      @RequestParam("file") MultipartFile file,
      @RequestParam("email") String email) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dataService.submitImageData(file, email));
  }
}
