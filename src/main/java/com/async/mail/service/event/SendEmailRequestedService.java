package com.async.mail.service.event;

import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.mail.Email;
import com.async.mail.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {

  private final Mailer mailer;

  @Async
  @EventListener
  @SneakyThrows
  @Override
  public void accept(SendEmailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());
    mailer.accept(
        new Email(recipientAddress, List.of(), List.of(), "", "... world!", List.of()));
  }
}
