package com.github.codogma.codogmaback.converter.image;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codogma.codogmaback.dto.PaletteDTO;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringToPalette implements Converter<String, PaletteDTO> {

  private final ObjectMapper objectMapper;

  @Override
  public PaletteDTO convert(@NonNull String source) {
    try {
      return objectMapper.readValue(source, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid JSON format for Palette: " + source, e);
    }
  }
}
