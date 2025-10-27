package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.model.ConfirmationTokenModel;
import com.github.codogma.codogmaback.repository.ConfirmationTokenRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {

  private final ConfirmationTokenRepository tokenRepository;

  @Transactional
  public void saveConfirmationToken(ConfirmationTokenModel token) {
    token.setCreatedAt(Instant.now());
    token.setExpiresAt(Instant.now().plusSeconds(24 * 3600L));
    tokenRepository.save(token);
  }

  public Optional<ConfirmationTokenModel> getToken(String token) {
    return tokenRepository.findByToken(token);
  }

  @Transactional
  public void setConfirmedAt(String token) {
    ConfirmationTokenModel confirmationToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalStateException("Token not found"));
    confirmationToken.setConfirmedAt(Instant.now());
    tokenRepository.save(confirmationToken);
  }
}