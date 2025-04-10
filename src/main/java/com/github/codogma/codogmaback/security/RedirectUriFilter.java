package com.github.codogma.codogmaback.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@Component
public class RedirectUriFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestURI = httpRequest.getRequestURI();
    log.warn("Request URI: {}", requestURI);
    if (requestURI.startsWith("/api/oauth2")) {
      String redirectUri = httpRequest.getParameter("redirect_success_uri");
      if (redirectUri != null) {
        log.info("Extracted redirect_success_uri: {}", redirectUri);
        httpRequest.getSession().setAttribute("redirect_success_uri", redirectUri);
      } else {
        log.info("No redirect_success_uri parameter found");
      }
    }
    //TODO обработать исключения InsufficientAuthenticationException (обработчик был добавлен в GlobalExceptionHandler, но не работает)
    chain.doFilter(request, response);
  }
}

