package com.async.mail.endpoint.event.model;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class SendEmailRequested extends PojaEvent {
  private String to;
  private String subject;
  private String htmlBody;
  private List<Attachment> attachments;

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  @Builder
  public static class Attachment {
    private String filename;
    private byte[] content;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(45);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
