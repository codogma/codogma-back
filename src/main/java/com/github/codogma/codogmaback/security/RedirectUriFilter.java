package com.github.codogma.codogmaback.security;

import com.github.codogma.codogmaback.exception.ExceptionFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RedirectUriFilter extends OncePerRequestFilter {

  private final ExceptionFactory exceptionFactory;

  public RedirectUriFilter(ExceptionFactory exceptionFactory) {
    this.exceptionFactory = exceptionFactory;
  }

  @Override
  public void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws IOException, ServletException {
    try {
      String requestURI = request.getRequestURI();
      log.warn("Request URI: {}", requestURI);
      if (requestURI.startsWith("/api/oauth2")) {
        String redirectUri = request.getParameter("redirect_success_uri");
        if (redirectUri != null) {
          log.info("Extracted redirect_success_uri: {}", redirectUri);
          request.getSession().setAttribute("redirect_success_uri", redirectUri);
        } else {
          log.info("No redirect_success_uri parameter found");
        }
      }
      filterChain.doFilter(request, response);
    } catch (InsufficientAuthenticationException ex) {
      log.error("Insufficient authentication exception caught", ex);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          exceptionFactory.insufficientAuthentication().getMessage());
    }
  }
}