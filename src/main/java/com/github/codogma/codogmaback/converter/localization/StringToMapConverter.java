package com.github.codogma.codogmaback.converter.localization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codogma.codogmaback.model.Language;
import java.io.IOException;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMapConverter implements Converter<String, Map<Language, String>> {

  private final ObjectMapper objectMapper;

  public StringToMapConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Map<Language, String> convert(String source) {
    try {
      return objectMapper.readValue(source, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid JSON format for Map: " + source, e);
    }
  }
}
