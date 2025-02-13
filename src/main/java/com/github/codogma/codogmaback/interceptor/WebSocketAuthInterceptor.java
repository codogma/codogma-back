package com.github.codogma.codogmaback.interceptor;

import com.github.codogma.codogmaback.service.JwtService;
import com.github.codogma.codogmaback.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    // Для команд, отличных от SUBSCRIBE, можно добавить проверку, если нужно
    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      String destination = accessor.getDestination();

      // Если подписка на публичный канал – пропускаем проверку
      if (destination != null && destination.startsWith("/topic/public")) {
        return message;
      }

      // Если пользователь уже установлен (аутентифицирован при CONNECT), пропускаем проверку
      if (accessor.getUser() != null) {
        return message;
      }

      // Если пользователь не аутентифицирован, пытаемся извлечь токен
      String token = TokenUtils.extractToken(accessor);
      if (token == null || token.trim().isEmpty()) {
        log.error("Missing authentication token for destination: {}", destination);
        throw new AuthenticationCredentialsNotFoundException("Missing authentication token");
      }

      String username = jwtService.extractUsername(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (!jwtService.isTokenValid(token, userDetails)) {
        log.error("Invalid authentication token for destination: {}", destination);
        throw new BadCredentialsException("Invalid authentication token");
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      accessor.setUser(auth);
    }
    return message;
  }
}
