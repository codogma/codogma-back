package com.github.codogma.codogmaback.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableCaching
public class CaffeineConfig {

  @Value("${spring.security.jwt.refresh-expiration}")
  private int refreshExpiration;

  @Bean("tokenCacheManager")
  public CacheManager tokenCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("revokedTokens");
    cacheManager.setCaffeine(caffeineCacheBuilder());
    return cacheManager;
  }

  private Caffeine<Object, Object> caffeineCacheBuilder() {
    return Caffeine.newBuilder().expireAfterWrite(refreshExpiration, TimeUnit.MILLISECONDS)
        .maximumSize(10000);
  }
}