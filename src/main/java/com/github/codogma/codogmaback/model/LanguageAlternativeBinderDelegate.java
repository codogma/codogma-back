package com.github.codogma.codogmaback.model;

import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.mapper.pojo.bridge.builtin.programmatic.AlternativeBinderDelegate;
import org.hibernate.search.mapper.pojo.bridge.builtin.programmatic.AlternativeValueBridge;
import org.hibernate.search.mapper.pojo.model.PojoModelProperty;

@RequiredArgsConstructor
public class LanguageAlternativeBinderDelegate implements
    AlternativeBinderDelegate<Language, String> {

  private final String name;

  @Override
  public AlternativeValueBridge<Language, String> bind(IndexSchemaElement indexSchemaElement,
      PojoModelProperty fieldValueSource) {
    EnumMap<Language, IndexFieldReference<String>> fields = new EnumMap<>(Language.class);
    String fieldNamePrefix = (name != null ? name : fieldValueSource.name()) + "_";

    for (Language language : Language.values()) {
      String languageCode = Language.fromCode(language.getCode()).getCode();
      IndexFieldReference<String> field = indexSchemaElement.field(fieldNamePrefix + languageCode,
          f -> f.asString().analyzer(languageCode)).toReference();
      fields.put(language, field);
    }

    return new Bridge(fields);
  }

  private record Bridge(EnumMap<Language, IndexFieldReference<String>> fields) implements
      AlternativeValueBridge<Language, String> {

    @Override
    public void write(DocumentElement target, Language discriminator, String bridgedElement) {
      if (discriminator == null) {
        discriminator = Language.EN;
      }
      target.addValue(fields.get(discriminator), bridgedElement);
    }
  }
}
