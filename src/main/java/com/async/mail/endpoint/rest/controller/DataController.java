package com.async.mail.endpoint.rest.controller;

import com.async.mail.endpoint.rest.controller.dto.DataListResponse;
import com.async.mail.endpoint.rest.controller.dto.DataResponse;
import com.async.mail.service.DataService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class DataController {

  private final DataService dataService;

  @GetMapping("/data")
  public ResponseEntity<DataListResponse> listImageData(
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String filename,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(dataService.listImageData(email, filename, page, size));
  }

  @PostMapping("/data")
  public ResponseEntity<DataResponse> submitImageData(
      @RequestPart("file") MultipartFile file, @RequestPart("email") String email) {
    return ResponseEntity.status(HttpStatus.CREATED).body(dataService.submitImageData(file, email));
  }
}
