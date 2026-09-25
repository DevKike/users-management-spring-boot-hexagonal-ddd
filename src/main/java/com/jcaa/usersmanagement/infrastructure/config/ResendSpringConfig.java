package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.email.ResendConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "email", name = "provider", havingValue = "resend")
public class ResendSpringConfig {

  private static final String PROP_RESEND_API_KEY      = "${resend.api-key}";
  private static final String PROP_RESEND_FROM_ADDRESS = "${resend.from.address}";
  private static final String PROP_RESEND_FROM_NAME    = "${resend.from.name}";

  @Value(PROP_RESEND_API_KEY)
  private String resendApiKey;

  @Value(PROP_RESEND_FROM_ADDRESS)
  private String resendFromAddress;

  @Value(PROP_RESEND_FROM_NAME)
  private String resendFromName;

  @Bean
  public ResendConfig resendConfig() {
    return new ResendConfig(resendApiKey, resendFromAddress, resendFromName);
  }
}
