package com.github.codogma.codogmaback.config;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.IndexSearcher;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
public class SearchIndexConfig implements ApplicationListener<ContextRefreshedEvent> {

  private final EntityManager entityManager;

  @Value("${search.mass.indexer.threads}")
  private int searchMassIndexerThreads;

  public SearchIndexConfig(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent event) {
    initializeSearchIndexing();
  }

  public void initializeSearchIndexing() {
    SearchSession searchSession = Search.session(entityManager);
    searchSession.massIndexer(ArticleModel.class, CategoryModel.class, UserModel.class,
            TagModel.class, CompilationModel.class).threadsToLoadObjects(searchMassIndexerThreads)
        .start().thenRun(() -> log.info("Indexing completed successfully.")).exceptionally(e -> {
          log.error("Error occurred during indexing.", e);
          return null;
        });
  }

  @PostConstruct
  public void increaseMaxClauseCount() {
    IndexSearcher.setMaxClauseCount(4096);
  }
}
