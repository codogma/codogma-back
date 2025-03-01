package com.github.codogma.codogmaback.util;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KeywordExtractor {

  private final String posModelPath;
  private final String lemmaModelPath;
  private POSModel posModel;
  private LemmatizerModel lemmatizerModel;
  private final ThreadLocal<POSTaggerME> posTagger;
  private final ThreadLocal<LemmatizerME> lemmatizer;
  private static final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
  private static final int BATCH_SIZE = 1000;
  private static final int MIN_LEMMA_LENGTH = 2;

  public KeywordExtractor(@Value("${opennlp-models.path.pos}") String posModelPath,
      @Value("${opennlp-models.path.lemmas}") String lemmaModelPath) {
    this.posModelPath = posModelPath;
    this.lemmaModelPath = lemmaModelPath;
    this.posTagger = ThreadLocal.withInitial(this::createPosTagger);
    this.lemmatizer = ThreadLocal.withInitial(this::createLemmatizer);
  }

  @PostConstruct
  private synchronized void initializeModels() {
    try {
      if (posModel == null) {
        Resource resourcePos = new ClassPathResource(posModelPath);
        try (InputStream posModelIn = resourcePos.getInputStream()) {
          posModel = new POSModel(posModelIn);
        }
      }
      if (lemmatizerModel == null) {
        Resource resourceLemmas = new ClassPathResource(lemmaModelPath);
        try (InputStream lemmasModelIn = resourceLemmas.getInputStream()) {
          lemmatizerModel = new LemmatizerModel(lemmasModelIn);
        }
      }
    } catch (IOException e) {
      log.error("Failed to initialize NLP models", e);
      throw new RuntimeException("Failed to initialize NLP models", e);
    }
  }

  private POSTaggerME createPosTagger() {
    return new POSTaggerME(posModel);
  }

  private LemmatizerME createLemmatizer() {
    return new LemmatizerME(lemmatizerModel);
  }

  public Set<String> extractKeywords(String text) {
    if (text == null || text.isEmpty()) {
      return Collections.emptySet();
    }
    long startTime = System.currentTimeMillis();
    try {
      String[] tokens = tokenizer.tokenize(text);
      return processTokens(tokens);
    } catch (Exception e) {
      log.error("Error extracting keywords", e);
      return Collections.emptySet();
    } finally {
      log.debug("Keyword extraction took {} ms", System.currentTimeMillis() - startTime);
    }
  }

  private Set<String> processTokens(String[] tokens) {
    int tokenCount = tokens.length;
    return IntStream.range(0, (tokenCount + BATCH_SIZE - 1) / BATCH_SIZE).parallel().mapToObj(
            i -> Arrays.stream(tokens, i * BATCH_SIZE, Math.min((i + 1) * BATCH_SIZE, tokenCount)))
        .flatMap(this::processBatch).collect(Collectors.toCollection(HashSet::new));
  }

  private boolean isValidEntry(Map.Entry<String, String> entry) {
    return (entry.getKey().startsWith("N") || entry.getKey().startsWith("X"))
        && entry.getValue().length() > MIN_LEMMA_LENGTH;
  }

  private Stream<String> processBatch(Stream<String> tokens) {
    POSTaggerME tagger = posTagger.get();
    LemmatizerME lem = lemmatizer.get();

    return tokens.map(token -> processToken(tagger, lem, token)).filter(this::isValidEntry)
        .map(Map.Entry::getValue);
  }

  private Map.Entry<String, String> processToken(POSTaggerME tagger, LemmatizerME lem,
      String token) {
    String[] singleToken = {token};
    String posTag = tagger.tag(singleToken)[0];
    String lemma = lem.lemmatize(singleToken, new String[]{posTag})[0];
    return new AbstractMap.SimpleEntry<>(posTag, lemma.toLowerCase());
  }
}
