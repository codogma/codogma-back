package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.model.ConfirmationToken;
import com.github.codogma.codogmaback.repository.ConfirmationTokenRepository;
import java.time.LocalDateTime;
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
  public void saveConfirmationToken(ConfirmationToken token) {
    token.setCreatedAt(LocalDateTime.now());
    token.setExpiresAt(LocalDateTime.now().plusHours(24));
    tokenRepository.save(token);
  }

  public Optional<ConfirmationToken> getToken(String token) {
    return tokenRepository.findByToken(token);
  }

  @Transactional
  public void setConfirmedAt(String token) {
    ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalStateException("Token not found"));
    confirmationToken.setConfirmedAt(LocalDateTime.now());
    tokenRepository.save(confirmationToken);
  }
}
