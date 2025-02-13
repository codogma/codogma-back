package com.github.codogma.codogmaback.security;

import static com.github.codogma.codogmaback.util.TokenUtils.extractToken;
import static com.github.codogma.codogmaback.util.TokenUtils.invalidateToken;

import com.github.codogma.codogmaback.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws IOException, ServletException {
    final String token = extractToken(request);

    try {
      if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (username != null && jwtService.isTokenValid(token, userDetails)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      invalidateToken(response);
    }

    filterChain.doFilter(request, response);
  }
}
