package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.exception.DeviceMismatchException;
import com.github.codogma.codogmaback.repository.RefreshTokenRepository;
import com.github.codogma.codogmaback.util.CookieUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceAwareService {

  private final RefreshTokenRepository refreshTokenRepository;
  @Value("${spring.security.jwt.device-claim-name}")
  private String deviceClaimName;
  @Value("${spring.security.jwt.device-salt}")
  private String deviceSalt;

  private final CookieUtils cookieUtils;

  @Transactional
  public void validateDeviceBinding(Claims claims, HttpServletRequest request,
      HttpServletResponse response) {
    String currentDeviceId = generateDeviceId(request);
    String tokenDeviceId = claims.get(deviceClaimName, String.class);
    String username = claims.getSubject();

    if (!MessageDigest.isEqual(currentDeviceId.getBytes(StandardCharsets.UTF_8),
        tokenDeviceId.getBytes(StandardCharsets.UTF_8))) {
      log.warn("Device mismatch: token={}, current={}, user={}", tokenDeviceId, currentDeviceId,
          username);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.addHeader("X-Security-Event", "device_mismatch");
      cookieUtils.invalidateAllTokens(response);
      refreshTokenRepository.deleteByUserUsername(username);
      throw new DeviceMismatchException("Device mismatch for user: " + username);
    }
  }

  public String generateDeviceId(HttpServletRequest request) {
    // Составные части идентификатора устройства
    String userAgent = request.getHeader("User-Agent");
    String timeZone = request.getHeader("Time-Zone");

    // Нормализация данных
    String normalizedUserAgent = userAgent != null ? userAgent : "";
    String normalizedTimeZone = timeZone != null ? timeZone : "";

    // Генерация хеша
    String compositeString = normalizedUserAgent + normalizedTimeZone + deviceSalt;

    return Sha512DigestUtils.shaHex(compositeString);
  }
}
