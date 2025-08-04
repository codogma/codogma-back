package com.github.codogma.codogmaback.util;

import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.Language;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class LocalizationUtil {

  private final MessageSource messageSource;
  private final LocalizationContext localizationContext;

  public Locale getLocale() {
    return Locale.forLanguageTag(localizationContext.getLanguage().getCode());
  }

  public String getMessage(String key, Object[] args) {
    Locale locale = getLocale();
    return messageSource.getMessage(key, args, locale);
  }

  public String getMessage(String key) {
    return getMessage(key, null);
  }

  public String getLocalizedValue(Map<Language, String> values) {
    Language interfaceLanguage = localizationContext.getLanguage();
    if (values.containsKey(interfaceLanguage) && StringUtils.hasText(
        values.get(interfaceLanguage))) {
      return values.get(interfaceLanguage);
    }
    if (values.containsKey(Language.EN) && StringUtils.hasText(
        values.get(interfaceLanguage))) {
      return values.get(Language.EN);
    }
    return values.values().stream().filter(StringUtils::hasText).findFirst().orElse(null);
  }

  public Map<Language, String> getLocalizedField(String key) {
    Map<Language, String> localizedField = new HashMap<>();
    for (Language language : Language.values()) {
      Locale locale = Locale.forLanguageTag(language.getCode());
      String value = messageSource.getMessage(key, null, locale);
      localizedField.put(language, value);
    }
    return localizedField;
  }
}