package com.github.codogma.codogmaback.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public class TokenUtils {

  public static String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("auth-token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public static String extractToken(StompHeaderAccessor accessor) {
    String token = accessor.getFirstNativeHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7).trim();
      if (!token.isEmpty()) {
        return token;
      }
    }
    List<String> cookieHeaders = accessor.getNativeHeader("Cookie");
    if (cookieHeaders != null) {
      for (String cookieHeader : cookieHeaders) {
        String[] cookies = cookieHeader.split(";\\s*");
        for (String cookie : cookies) {
          if (cookie.startsWith("auth-token=")) {
            String val = cookie.substring("auth-token=".length()).trim();
            if (!val.isEmpty()) {
              return val;
            }
          }
        }
      }
    }
    return null;
  }

  public static void setAuthCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = ResponseCookie.from("auth-token", token).httpOnly(true).secure(false)
        .path("/").build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public static void invalidateToken(HttpServletResponse response) {
    ResponseCookie authTokenCookie = ResponseCookie.from("auth-token", "").httpOnly(true)
        .secure(false).path("/").maxAge(0).build();
    response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());
    ResponseCookie userCookie = ResponseCookie.from("user", "").httpOnly(true).secure(false)
        .path("/").maxAge(0).build();
    response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());
  }
}
