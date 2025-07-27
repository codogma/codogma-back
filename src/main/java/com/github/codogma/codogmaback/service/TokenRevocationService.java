package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.model.RefreshTokenModel;
import com.github.codogma.codogmaback.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

  private final RefreshTokenRepository refreshTokenRepository;

  public void revokeToken(String jti) {
    refreshTokenRepository.findByTokenHash(jti)
        .ifPresent(token -> {
          token.setRevoked(true);
          refreshTokenRepository.save(token);
          cacheRevokedToken(jti, true);
        });
  }

  @Cacheable(value = "revokedTokens", key = "#jti", cacheManager = "tokenCacheManager")
  public boolean isTokenRevoked(String jti) {
    return refreshTokenRepository.findByTokenHash(jti)
        .map(RefreshTokenModel::isRevoked)
        .orElse(false);
  }

  @CachePut(value = "revokedTokens", key = "#jti", cacheManager = "tokenCacheManager")
  public boolean cacheRevokedToken(String jti, boolean revoked) {
    return revoked;
  }
}
