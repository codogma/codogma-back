package com.github.codogma.codogmaback.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;
  @Value("${spring.security.jwt.access-expiration}")
  private int accessExpiration;
  @Value("${spring.security.jwt.refresh-expiration}")
  private int refreshExpiration;
  @Value("${spring.security.jwt.access-token-name}")
  private String accessTokenName;
  @Value("${spring.security.jwt.refresh-token-name}")
  private String refreshTokenName;
  @Value("${spring.security.jwt.same-site}")
  private String sameSite;
  @Value("${spring.security.jwt.domain}")
  private String domain;

  public String extractAccessToken(HttpServletRequest request) {
    return extractToken(request, accessTokenName);
  }

  public String extractRefreshToken(HttpServletRequest request) {
    return extractToken(request, refreshTokenName);
  }

  private String extractToken(HttpServletRequest request, String tokenName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (tokenName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public String extractAccessToken(StompHeaderAccessor accessor) {
    return extractTokenFromHeaders(accessor, accessTokenName);
  }

  public String extractRefreshToken(StompHeaderAccessor accessor) {
    return extractTokenFromHeaders(accessor, refreshTokenName);
  }

  private String extractTokenFromHeaders(StompHeaderAccessor accessor, String tokenName) {
    String cookieHeader = accessor.getFirstNativeHeader("Cookie");
    if (cookieHeader == null) {
      return null;
    }
    String[] cookies = cookieHeader.split(";\\s*");
    for (String cookie : cookies) {
      if (cookie.startsWith(tokenName + "=")) {
        String val = cookie.substring((tokenName + "=").length()).trim();
        if (!val.isEmpty()) {
          return val;
        }
      }
    }
    return null;
  }

  public String extractAccessToken(ServerHttpRequest request) {
    return extractTokenFromServerHttpRequest(request, accessTokenName);
  }

  public String extractRefreshToken(ServerHttpRequest request) {
    return extractTokenFromServerHttpRequest(request, refreshTokenName);
  }

  private String extractTokenFromServerHttpRequest(ServerHttpRequest request, String tokenName) {
    String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
    if (cookieHeader == null) {
      return null;
    }

    String[] cookies = cookieHeader.split(";\\s*");
    for (String cookie : cookies) {
      cookie = cookie.trim();
      if (cookie.startsWith(tokenName + "=")) {
        return cookie.substring(tokenName.length() + 1);
      }
    }
    return null;
  }

  private ResponseCookie createHttpOnlyCookie(String name, String token, long maxAge,
      String domain) {
    boolean isProduction = activeProfile.contains("prod");
    return ResponseCookie.from(name, token).httpOnly(true).secure(isProduction).path("/")
        .maxAge(maxAge).sameSite(sameSite).domain(isProduction ? domain : null).build();
  }

  public void invalidateAllTokens(HttpServletResponse response) {
    ResponseCookie accessTokenCookie = createHttpOnlyCookie(accessTokenName, "", 0, "");
    response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

    ResponseCookie refreshTokenCookie = createHttpOnlyCookie(refreshTokenName, "", 0, "");
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
  }

  public void setAccessTokenToHttpOnlyCookie(HttpServletResponse response, String accessToken) {
    response.addHeader(HttpHeaders.SET_COOKIE,
        createHttpOnlyCookie(accessTokenName, accessToken, accessExpiration, domain).toString());
  }

  public void setRefreshTokenToHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
    response.addHeader(HttpHeaders.SET_COOKIE,
        createHttpOnlyCookie(refreshTokenName, refreshToken, refreshExpiration, domain).toString());
  }
}
