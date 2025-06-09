package com.github.codogma.codogmaback.converter.localization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codogma.codogmaback.model.Language;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringToMap implements Converter<String, Map<Language, String>> {

  private final ObjectMapper objectMapper;

  @Override
  public Map<Language, String> convert(@NonNull String source) {
    try {
      return objectMapper.readValue(source, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid JSON format for Map: " + source, e);
    }
  }
}
