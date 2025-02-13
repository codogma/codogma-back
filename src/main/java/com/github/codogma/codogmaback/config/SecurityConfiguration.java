package com.github.codogma.codogmaback.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.github.codogma.codogmaback.handler.oauth.CustomOAuth2SuccessHandler;
import com.github.codogma.codogmaback.security.ExceptionHandlingFilter;
import com.github.codogma.codogmaback.security.JwtAuthenticationFilter;
import com.github.codogma.codogmaback.security.RedirectUriFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ExceptionHandlingFilter exceptionHandlingFilter;
  private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
  private final RedirectUriFilter redirectUriFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource())).authorizeHttpRequests(
            auth -> auth.requestMatchers("/auth/**", "/swagger-ui.html", "/swagger-ui/**",
                    "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles/**", "/categories/**", "/users/**",
                    "/compilations/**", "/images/**", "/comments/**", "/tags/**",
                    "/notifications/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/articles/*/record-view").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .anyRequest().authenticated()).oauth2Login(
            oauth2 -> oauth2.successHandler(customOAuth2SuccessHandler).userInfoEndpoint(
                userInfo -> userInfo.userAuthoritiesMapper(new SimpleAuthorityMapper())))
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint((request, response, authException) -> {
              throw authException;
            }).accessDeniedHandler((request, response, accessDeniedException) -> {
              throw accessDeniedException;
            })).authenticationProvider(authenticationProvider)
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .addFilterBefore(redirectUriFilter, OAuth2AuthorizationRequestRedirectFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(exceptionHandlingFilter, ExceptionTranslationFilter.class).build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
    corsConfiguration.setAllowedMethods(List.of("*"));
    corsConfiguration.setAllowedHeaders(List.of("*"));
    corsConfiguration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }
}