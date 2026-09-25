package com.jcaa.usersmanagement.infrastructure.adapter.email;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "email", name = "provider", havingValue = "resend")
public class ResendEmailSenderAdapter implements EmailSenderPort {

  private static final String RESEND_BASE_URL = "https://api.resend.com";
  private static final String RESEND_EMAILS_PATH = "/emails";
  private static final String AUTH_HEADER_PREFIX = "Bearer ";
  private static final int TIMEOUT_MILLIS = 10_000;
  private static final String LOG_SENT = "[ResendEmailSenderAdapter] correo enviado exitosamente.";

  private final RestClient restClient;
  private final String from;

  public ResendEmailSenderAdapter(final ResendConfig config) {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(TIMEOUT_MILLIS);
    requestFactory.setReadTimeout(TIMEOUT_MILLIS);
    this.restClient =
        RestClient.builder()
            .baseUrl(RESEND_BASE_URL)
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + config.apiKey())
            .build();
    this.from = config.fromName() + " <" + config.fromAddress() + ">";
  }

  @Override
  public void send(final EmailDestinationModel destination) {
    try {
      restClient
          .post()
          .uri(RESEND_EMAILS_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .body(ResendEmailRequest.from(from, destination))
          .retrieve()
          .toBodilessEntity();
      log.info(LOG_SENT);
    } catch (final RestClientException restClientException) {
      throw EmailSenderException.becauseSmtpFailed(
          destination.getDestinationEmail(), restClientException.getMessage());
    }
  }

  private record ResendEmailRequest(String from, List<String> to, String subject, String html) {

    private static ResendEmailRequest from(
        final String from, final EmailDestinationModel destination) {
      return new ResendEmailRequest(
          from,
          List.of(destination.getDestinationEmail()),
          destination.getSubject(),
          destination.getBody());
    }
  }
}
