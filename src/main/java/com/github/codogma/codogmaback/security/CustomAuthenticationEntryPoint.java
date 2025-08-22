package com.github.codogma.codogmaback.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.addHeader("X-Security-Event", "access_token_expired");
    response.setContentType("application/json");
    Map<String, Object> error = new HashMap<>();
    error.put("errorCode", "Unauthorized");
    error.put("message", "Authentication required or token expired");
    new ObjectMapper().writeValue(response.getWriter(), error);
    log.warn("AuthenticationEntryPoint triggered: {}", authException.getMessage());
  }
}
