package com.github.codogma.codogmaback.converter.localization;

import com.github.codogma.codogmaback.model.Language;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToLanguageConverter implements Converter<String, Language> {

  @Override
  public Language convert(@NonNull String source) {
    try {
      return Language.fromCode(source);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid language value: " + source);
    }
  }
}
