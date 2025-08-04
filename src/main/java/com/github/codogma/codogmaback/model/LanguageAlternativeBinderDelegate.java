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
  public AlternativeValueBridge<Language, String> bind(final IndexSchemaElement indexSchemaElement,
      final PojoModelProperty fieldValueSource) {
    final EnumMap<Language, IndexFieldReference<String>> fields = new EnumMap<>(Language.class);
    final String fieldNamePrefix = (null != name ? this.name : fieldValueSource.name()) + "_";

    for (final Language language : Language.values()) {
      final String languageCode = Language.fromCode(language.getCode()).getCode();
      final IndexFieldReference<String> field = indexSchemaElement.field(fieldNamePrefix + languageCode,
          f -> f.asString().analyzer(languageCode)).toReference();
      fields.put(language, field);
    }

    return new Bridge(fields);
  }

  private record Bridge(EnumMap<Language, IndexFieldReference<String>> fields) implements
      AlternativeValueBridge<Language, String> {

    @Override
    public void write(final DocumentElement target, Language discriminator, final String bridgedElement) {
      if (null == discriminator) {
        discriminator = Language.EN;
      }
      target.addValue(this.fields.get(discriminator), bridgedElement);
    }
  }
}