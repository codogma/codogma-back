package com.github.codogma.codogmaback.security;

import com.github.codogma.codogmaback.exception.DeviceMismatchException;
import com.github.codogma.codogmaback.exception.RevokedTokenException;
import com.github.codogma.codogmaback.service.DeviceAwareService;
import com.github.codogma.codogmaback.service.TokenRevocationService;
import com.github.codogma.codogmaback.util.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final CookieUtils cookieUtils;
  private final DeviceAwareService deviceAwareService;
  private final UserDetailsService userDetailsService;
  private final TokenRevocationService tokenRevocationService;

  private static final List<String> PUBLIC_PATHS = Arrays.asList("/swagger-ui", "/v3/api-docs",
      "/auth", "/ws", "/error", "/swagger-resources");

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String servletPath = request.getServletPath();
    return PUBLIC_PATHS.stream().anyMatch(servletPath::startsWith);
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws IOException, ServletException {
    try {
      final String accessToken = cookieUtils.extractAccessToken(request);
      final String refreshToken = cookieUtils.extractRefreshToken(request);
      log.warn("Access token: {}, refresh token: {}", accessToken, refreshToken);
      if (accessToken != null && jwtProvider.isTokenValid(accessToken))
//          || refreshToken != null && jwtProvider.isTokenValid(refreshToken))
      {
        Claims claims = jwtProvider.extractAllClaims(accessToken);

        // Check if token has been revoked
        String jti = claims.getId();
        if (tokenRevocationService.isTokenRevoked(jti)) {
          throw new RevokedTokenException("Access token revoked");
        }

        deviceAwareService.validateDeviceBinding(claims, request, response);
        String username = jwtProvider.extractUsername(accessToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException ex) {
      log.debug("Access token expired: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (DeviceMismatchException ex) {
      log.debug(ex.getMessage());
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.addHeader("X-Security-Event", "device_mismatch");
    } catch (RevokedTokenException ex) {
      log.warn("Revoked JWT token: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      throw new AuthenticationServiceException("Token revoked", ex);
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("Invalid JWT token: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
