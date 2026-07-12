package com.async.mail.service;

import com.async.mail.entity.User;
import com.async.mail.repository.model.JCourse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class InvoiceService {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

  private static String invoiceTemplate() {
    try {
      return new String(
          new ClassPathResource("email/invoice.html")
              .getInputStream()
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load invoice template", e);
    }
  }

  public byte[] generateInvoice(
      User user, JCourse course, String invoiceNumber) {
    var date = DATE_FORMATTER.format(Instant.now());
    var priceFormatted =
        NumberFormat.getCurrencyInstance(Locale.US).format(course.getPrice());
    var html =
        invoiceTemplate()
            .formatted(
                invoiceNumber,
                date,
                user.firstName(),
                user.lastName(),
                user.email(),
                course.getTitle(),
                priceFormatted,
                priceFormatted);
    return renderToPdf(html);
  }

  private byte[] renderToPdf(String xhtml) {
    try {
      var os = new ByteArrayOutputStream();
      var renderer = new ITextRenderer();
      renderer.setDocumentFromString(xhtml);
      renderer.layout();
      renderer.createPDF(os);
      return os.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Failed to render invoice PDF", e);
    }
  }
}
