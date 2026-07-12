package com.async.mail.service;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@AllArgsConstructor
public class S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${app.invoice.s3-bucket}")
  private String invoiceBucket;

  public String uploadInvoice(UUID userId, UUID courseId, byte[] pdfContent) {
    var key =
        String.format(
            "invoices/%s/%s/%d.pdf", userId, courseId, System.currentTimeMillis());
    var request =
        PutObjectRequest.builder()
            .bucket(invoiceBucket)
            .key(key)
            .contentType("application/pdf")
            .build();
    s3Client.putObject(request, RequestBody.fromBytes(pdfContent));
    return key;
  }

  public URL generateDownloadUrl(String key) {
    var getObjectRequest =
        GetObjectRequest.builder().bucket(invoiceBucket).key(key).build();
    var presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofDays(7))
            .getObjectRequest(getObjectRequest)
            .build();
    return s3Presigner.presignGetObject(presignRequest).url();
  }
}
