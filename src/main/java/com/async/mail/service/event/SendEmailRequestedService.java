package com.async.mail.service.event;

import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.mail.Email;
import com.async.mail.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {

  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SendEmailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());
    var subject = sendEmailRequested.getSubject() != null ? sendEmailRequested.getSubject() : "";
    var htmlBody =
        sendEmailRequested.getHtmlBody() != null ? sendEmailRequested.getHtmlBody() : "... world!";
    mailer.accept(new Email(recipientAddress, List.of(), List.of(), subject, htmlBody, List.of()));
  }
}
