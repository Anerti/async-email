package com.async.mail.service.event;

import com.async.mail.endpoint.event.model.SendEmailRequested;
import com.async.mail.mail.Email;
import com.async.mail.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
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
    var attachments = toFiles(sendEmailRequested.getAttachments());
    mailer.accept(new Email(recipientAddress, List.of(), List.of(), subject, htmlBody, attachments));
  }

  private List<File> toFiles(List<SendEmailRequested.Attachment> attachments) {
    if (attachments == null) {
      return List.of();
    }
    var files = new ArrayList<File>(attachments.size());
    for (var attachment : attachments) {
      files.add(toTempFile(attachment));
    }
    return files;
  }

  @SneakyThrows
  private File toTempFile(SendEmailRequested.Attachment attachment) {
    var tempFile = File.createTempFile("att-", "-" + attachment.getFilename());
    Files.write(tempFile.toPath(), attachment.getContent());
    tempFile.deleteOnExit();
    return tempFile;
  }
}
