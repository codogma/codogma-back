package com.github.codogma.codogmaback.config;

import com.github.codogma.codogmaback.interceptor.localization.CustomLocaleResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
@RequiredArgsConstructor
public class LocaleConfig {

  private final CustomLocaleResolver customLocaleResolver;

  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("i18n/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  @Bean
  public LocaleResolver localeResolver() {
    return customLocaleResolver;
  }
}