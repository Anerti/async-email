package com.async.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Conf {

  @Bean
  public S3Client s3Client(@Value("${app.invoice.s3-region:eu-west-3}") Region region) {
    return S3Client.builder().region(region).build();
  }

  @Bean
  public S3Presigner s3Presigner(@Value("${app.invoice.s3-region:eu-west-3}") Region region) {
    return S3Presigner.builder().region(region).build();
  }
}
