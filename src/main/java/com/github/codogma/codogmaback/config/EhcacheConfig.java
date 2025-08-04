package com.github.codogma.codogmaback.config;

import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetComment;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.GetNotification;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.GetUser;
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
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;

@Configuration
@EnableCaching
public class EhcacheConfig {

  @Value("${spring.cache.jcache.tti-minutes}")
  private int tti;
  @Value("${spring.cache.jcache.max-heap-entries}")
  private int entries;

  @Bean
  @Primary
  public JCacheCacheManager jCacheManager() {
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

    // Конфигурация для категории
    CacheConfiguration<Object, GetCategory> categoryByIdConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, GetCategory.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для категорий
    CacheConfiguration<Object, Page<GetCategory>> categoriesConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetCategory>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для категорий по названию
    CacheConfiguration<Object, List<GetCategory>> categoriesByNameConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<List<GetCategory>>) (Class<?>) List.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для статей
    CacheConfiguration<Object, Page<GetComment>> commentsConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetComment>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для подборки
    CacheConfiguration<Object, GetCompilation> compilationByIdConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, GetCompilation.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для подборок
    CacheConfiguration<Object, Page<GetCompilation>> compilationsConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetCompilation>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для подборок по заголовку
    CacheConfiguration<Object, List<GetCompilation>> compilationsByTitleConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<List<GetCompilation>>) (Class<?>) List.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для подборок
    CacheConfiguration<Object, Page<GetNotification>> notificationsConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetNotification>>) (Class<?>) Page.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti)))
        .withResourcePools(ResourcePoolsBuilder.heap(100)).build();

    // Конфигурация для тегов по названию
    CacheConfiguration<Object, List<GetTag>> tagsByNameConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<List<GetTag>>) (Class<?>) List.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для пользователя
    CacheConfiguration<Object, GetUser> userByUsernameConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, GetUser.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder().heap(entries, EntryUnit.ENTRIES))
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(tti))).build();

    // Конфигурация для пользователей
    CacheConfiguration<Object, Page<GetUser>> usersConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, (Class<Page<GetUser>>) (Class<?>) Page.class,
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
    jCacheManager.createCache("categories",
        Eh107Configuration.fromEhcacheCacheConfiguration(categoriesConfig));
    jCacheManager.createCache("categoriesByName",
        Eh107Configuration.fromEhcacheCacheConfiguration(categoriesByNameConfig));
    jCacheManager.createCache("categoryById",
        Eh107Configuration.fromEhcacheCacheConfiguration(categoryByIdConfig));
    jCacheManager.createCache("recommendations",
        Eh107Configuration.fromEhcacheCacheConfiguration(recommendationsConfig));
    jCacheManager.createCache("comments",
        Eh107Configuration.fromEhcacheCacheConfiguration(commentsConfig));
    jCacheManager.createCache("compilationById",
        Eh107Configuration.fromEhcacheCacheConfiguration(compilationByIdConfig));
    jCacheManager.createCache("compilations",
        Eh107Configuration.fromEhcacheCacheConfiguration(compilationsConfig));
    jCacheManager.createCache("compilationsByTitle",
        Eh107Configuration.fromEhcacheCacheConfiguration(compilationsByTitleConfig));
    jCacheManager.createCache("notifications",
        Eh107Configuration.fromEhcacheCacheConfiguration(notificationsConfig));
    jCacheManager.createCache("tagsByName",
        Eh107Configuration.fromEhcacheCacheConfiguration(tagsByNameConfig));
    jCacheManager.createCache("userByUsername",
        Eh107Configuration.fromEhcacheCacheConfiguration(userByUsernameConfig));
    jCacheManager.createCache("users",
        Eh107Configuration.fromEhcacheCacheConfiguration(usersConfig));

    return new JCacheCacheManager(jCacheManager);
  }
}