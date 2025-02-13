package com.github.codogma.codogmaback.config;

import com.github.codogma.codogmaback.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic",
        "/queue"); // Включаем простой брокер для топиков и очередей
    registry.setApplicationDestinationPrefixes("/app"); // Клиенты отправляют в /app/*
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // Основная точка входа для WebSocket
        .setAllowedOriginPatterns("*") // Поддержка CORS (настроить под нужный домен)
        .withSockJS(); // Включаем SockJS (фолбэк для браузеров без WebSocket)
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // Регистрируем интерцептор для аутентификации по JWT в сообщениях
    registration.interceptors(webSocketAuthInterceptor);
  }
}
