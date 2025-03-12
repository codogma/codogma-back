package com.github.codogma.codogmaback.config;

import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetComment;
import java.time.Duration;
import java.util.List;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

@Configuration
@EnableCaching
public class EhcacheConfig {

  @Value("${spring.cache.jcache.tti-minutes}")
  private int tti;
  @Value("${spring.cache.jcache.max-heap-entries}")
  private int entries;

  @Bean
  public JCacheCacheManager cacheManager() {
    // Конфигурация для статьи
    CacheConfiguration<Object, GetArticle> articleByIdConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, GetArticle.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для статей
    CacheConfiguration<Object, Page<GetArticle>> articlesConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetArticle>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для просмотренных статей
    CacheConfiguration<Object, Page<GetArticle>> viewedArticlesConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetArticle>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для рекомендаций
    CacheConfiguration<Long, List<GetArticle>> recommendationsConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, (Class<List<GetArticle>>) (Class<?>) List.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для статей
    CacheConfiguration<Object, Page<GetComment>> commentsConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetComment>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    CachingProvider provider = Caching.getCachingProvider();
    CacheManager jCacheManager = provider.getCacheManager();

    // Создаем кэши
    jCacheManager.createCache("articleById",
        Eh107Configuration.fromEhcacheCacheConfiguration(articleByIdConfig));
    jCacheManager.createCache("articles",
        Eh107Configuration.fromEhcacheCacheConfiguration(articlesConfig));
    jCacheManager.createCache("viewedArticles",
        Eh107Configuration.fromEhcacheCacheConfiguration(viewedArticlesConfig));
    jCacheManager.createCache("recommendations",
        Eh107Configuration.fromEhcacheCacheConfiguration(recommendationsConfig));
    jCacheManager.createCache("comments",
        Eh107Configuration.fromEhcacheCacheConfiguration(commentsConfig));

    return new JCacheCacheManager(jCacheManager);
  }
}
