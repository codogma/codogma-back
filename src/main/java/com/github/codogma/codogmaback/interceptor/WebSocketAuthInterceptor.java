package com.github.codogma.codogmaback.interceptor;

import com.github.codogma.codogmaback.security.JwtProvider;
import com.github.codogma.codogmaback.service.DeviceAwareService;
import com.github.codogma.codogmaback.service.TokenRevocationService;
import com.github.codogma.codogmaback.util.CookieUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  @Value("${spring.security.jwt.device-claim-name}")
  private String deviceClaimName;

  private final JwtProvider jwtProvider;
  private final CookieUtils cookieUtils;
  private final UserDetailsService userDetailsService;
  private final TokenRevocationService tokenRevocationService;
  private final DeviceAwareService deviceAwareService;


  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    String destination = accessor.getDestination();

    if (destination != null && destination.startsWith("/topic/public")) {
      return message;
    }

    // Для команд, отличных от SUBSCRIBE, можно добавить проверку, если нужно
    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

      // Если подписка на публичный канал – пропускаем проверку
      if (destination != null && destination.startsWith("/topic/public")) {
        return message;
      }

      // Если пользователь уже установлен (аутентифицирован при CONNECT), пропускаем проверку
      if (accessor.getUser() != null) {
        return message;
      }

      // Если пользователь не аутентифицирован, пытаемся извлечь токен
      String accessToken = cookieUtils.extractAccessToken(accessor);
      if (accessToken == null || accessToken.trim().isEmpty()) {
        log.error("Missing authentication accessToken for destination: {}", destination);
        throw new AuthenticationCredentialsNotFoundException("Missing authentication accessToken");
      }

      String username = jwtProvider.extractUsername(accessToken);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (!jwtProvider.isTokenValid(accessToken)) {
        log.error("Invalid authentication accessToken for destination: {}", destination);
        throw new BadCredentialsException("Invalid authentication accessToken");
      }

      Claims claims = jwtProvider.extractAccessTokenClaims(accessToken);

      String jti = claims.getId();
      if (tokenRevocationService.isTokenRevoked(jti)) {
        log.error("Revoked authentication accessToken for destination: {}", destination);
        throw new BadCredentialsException("Revoked authentication accessToken");
      }

      String tokenDeviceId = claims.get(deviceClaimName, String.class);
      String currentDeviceId = deviceAwareService.generateDeviceId(accessor);

      if (!tokenDeviceId.equals(currentDeviceId)) {
        log.error("Device binding mismatch for destination: {}", destination);
        throw new BadCredentialsException("Device binding mismatch");
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      accessor.setUser(auth);
    }
    return message;
  }
}