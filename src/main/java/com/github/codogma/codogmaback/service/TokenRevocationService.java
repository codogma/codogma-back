package com.github.codogma.codogmaback.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.codogma.codogmaback.model.RefreshTokenModel;
import com.github.codogma.codogmaback.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

  @Value("${spring.security.jwt.refresh-expiration}")
  private int refreshExpiration;

  private final Cache<String, Instant> revokedTokens = Caffeine.newBuilder()
      .expireAfterWrite(refreshExpiration, TimeUnit.MILLISECONDS).build();
  private final RefreshTokenRepository refreshTokenRepository;

  public void revokeToken(String jti) {
    refreshTokenRepository.findByTokenHash(jti)
        .ifPresent(token -> {
          token.setRevoked(true);
          refreshTokenRepository.save(token);
          revokedTokens.put(jti, Instant.now());
        });
  }

  public boolean isTokenRevoked(String jti) {
    Instant cachedRevocation = revokedTokens.getIfPresent(jti);
    if (cachedRevocation != null) {
      return true;
    }

    return refreshTokenRepository.findByTokenHash(jti)
        .map(RefreshTokenModel::isRevoked)
        .orElse(false);
  }
}
