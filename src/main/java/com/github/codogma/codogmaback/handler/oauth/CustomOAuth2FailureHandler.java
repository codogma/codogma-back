package com.github.codogma.codogmaback.handler.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    String redirectUrl = (String) request.getSession().getAttribute("redirect_error_uri");

    if (redirectUrl == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("OAuth2 authentication failed: " + exception.getMessage());
      return;
    }

    try {
      redirectUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      log.error("Error decoding redirect error URI: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid redirect error URI");
      return;
    }

    log.info("OAuth2 auth failed: redirecting to error page: {}", redirectUrl);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}
