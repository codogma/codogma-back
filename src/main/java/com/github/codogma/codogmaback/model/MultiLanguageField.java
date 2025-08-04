package com.github.codogma.codogmaback.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.search.engine.environment.bean.BeanReference;
import org.hibernate.search.mapper.pojo.bridge.builtin.programmatic.AlternativeBinder;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMapping;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessorContext;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessorRef;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@PropertyMapping(processor = @PropertyMappingAnnotationProcessorRef(
    type = MultiLanguageField.Processor.class
))
@Documented
public @interface MultiLanguageField {

  String name() default "";

  class Processor
      implements PropertyMappingAnnotationProcessor<MultiLanguageField> {

    @Override
    public void process(final PropertyMappingStep mapping, final MultiLanguageField annotation,
        final PropertyMappingAnnotationProcessorContext context) {
      final LanguageAlternativeBinderDelegate delegate = new LanguageAlternativeBinderDelegate(
          annotation.name().isEmpty() ? null : annotation.name()
      );
      mapping.hostingType()
          .binder(AlternativeBinder.create(
              Language.class,
              context.annotatedElement().name(),
              String.class,
              BeanReference.ofInstance(delegate)
          ));
    }
  }
}