package com.async.mail.endpoint.rest.controller.health;

import static com.async.mail.endpoint.rest.controller.health.PingController.OK;

import com.async.mail.PojaGenerated;
import com.async.mail.endpoint.event.EventProducer;
import com.async.mail.endpoint.event.model.SendEmailRequested;
import jakarta.mail.internet.AddressException;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class HealthEmailController {

  private final EventProducer<SendEmailRequested> eventProducer;

  @GetMapping(value = "/health/email")
  public ResponseEntity<String> send_emails(@RequestParam String to)
      throws AddressException, IOException {
    eventProducer.accept(
        List.of(
            SendEmailRequested.builder().to(to).build(),
            SendEmailRequested.builder().to(to.split("@")[0] + "+cc@" + to.split("@")[1]).build(),
            SendEmailRequested.builder().to(to.split("@")[0] + "+bcc@" + to.split("@")[1]).build(),
            SendEmailRequested.builder().to(to).build(),
            SendEmailRequested.builder().to(to).build()));
    return OK;
  }
}
