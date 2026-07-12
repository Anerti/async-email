package com.async.mail.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

  public String generateQrDataUri(String url) {
    return generateQrDataUri(url, 200);
  }

  public String generateQrDataUri(String url, int size) {
    try {
      var writer = new QRCodeWriter();
      var bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size);
      var outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      var base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
      return "data:image/png;base64," + base64;
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate QR code", e);
    }
  }
}
