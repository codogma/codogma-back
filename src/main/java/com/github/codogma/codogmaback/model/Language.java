package com.github.codogma.codogmaback.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
  EN("en"), RU("ru");

  private final String code;

  @JsonCreator
  public static Language fromCode(final String code) {
    return Stream.of(Language.values()).filter(lang -> null != lang && lang.code.equalsIgnoreCase(code))
        .findFirst().orElse(null);
  }

  @JsonValue
  public String getCode() {
    return this.code;
  }
}