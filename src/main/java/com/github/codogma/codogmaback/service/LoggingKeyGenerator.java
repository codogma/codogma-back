package com.github.codogma.codogmaback.service;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggingKeyGenerator")
public class LoggingKeyGenerator implements KeyGenerator {

  @NonNull
  @Override
  public Object generate(@NonNull Object target, Method method, @NonNull Object... params) {
    Object key = new SimpleKey(params);
    log.info("Generated cache key for method {}: {}", method.getName(), key);
    return key;
  }
}
