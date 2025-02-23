package com.github.codogma.codogmaback.config;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchAnalysisConfigurer implements LuceneAnalysisConfigurer {

  @Override
  public void configure(LuceneAnalysisConfigurationContext context) {
    context.analyzer("en").custom()
        .tokenizer("standard")
        .charFilter("htmlStrip")
        .tokenFilter("lowercase")
        .tokenFilter("snowballPorter")
        .param("language", "English")
        .tokenFilter("englishPossessive")
        .tokenFilter("porterStem")
        .tokenFilter("asciiFolding")
        .tokenFilter("edgeNGram")
        .param("minGramSize", "3")
        .param("maxGramSize", "15");

    context.normalizer("exact").custom()
        .tokenFilter("lowercase")
        .tokenFilter("asciiFolding");

    context.analyzer("ru").custom()
        .tokenizer("standard")
        .charFilter("htmlStrip")
        .tokenFilter("lowercase")
        .tokenFilter("snowballPorter")
        .param("language", "Russian")
        .tokenFilter("russianLightStem")
        .tokenFilter("asciiFolding")
        .tokenFilter("edgeNGram")
        .param("minGramSize", "3")
        .param("maxGramSize", "15");
  }
}
