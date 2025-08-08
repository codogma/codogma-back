package com.github.codogma.codogmaback.handler.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    String redirectUrl = (String) request.getSession().getAttribute("redirect_success_uri");

    if (redirectUrl == null) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("OAuth2 authentication successful");
      return;
    }

    try {
      redirectUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      log.error("Error decoding redirect URI: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid redirect URI");
      return;
    }

    log.info("OAuth2 auth successful: redirecting to success page: {}", redirectUrl);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}