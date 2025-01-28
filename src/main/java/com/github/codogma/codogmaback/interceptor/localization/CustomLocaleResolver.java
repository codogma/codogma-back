package com.github.codogma.codogmaback.interceptor.localization;

import com.github.codogma.codogmaback.model.Language;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

@Component
@RequiredArgsConstructor
public class CustomLocaleResolver implements LocaleResolver {

  private final LocalizationContext localizationContext;

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    Language localeCode = localizationContext.getLanguage();
    if (localeCode == null) {
      return Locale.ENGLISH;
    }
    return Locale.forLanguageTag(localeCode.getCode());
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
  }
}
