package com.github.codogma.codogmaback.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.github.codogma.codogmaback.handler.oauth.CustomOAuth2SuccessHandler;
import com.github.codogma.codogmaback.security.CustomAuthenticationEntryPoint;
import com.github.codogma.codogmaback.security.ExceptionHandlingFilter;
import com.github.codogma.codogmaback.security.JwtAuthenticationFilter;
import com.github.codogma.codogmaback.security.RedirectUriFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

  @Value("${cors.max-age:300}")
  private long maxAge;
  private final CorsProperties corsProperties;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ExceptionHandlingFilter exceptionHandlingFilter;
  private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
  private final RedirectUriFilter redirectUriFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .anonymous(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**",
                    "/v3/api-docs/**", "/auth/**", "/ws/**", "/error", "/swagger-resources/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/articles/**", "/categories/**", "/users/**",
                    "/compilations/**", "/images/**", "/comments/**", "/tags/**",
                    "/notifications/**", "/recommendations/**").permitAll().anyRequest()
                .authenticated()).oauth2Login(
            oauth2 -> oauth2.successHandler(customOAuth2SuccessHandler).userInfoEndpoint(
                userInfo -> userInfo.userAuthoritiesMapper(new SimpleAuthorityMapper())))
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
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
    List<String> allowedOriginPatterns = corsProperties.getAllowedOriginPatterns();
    corsConfiguration.setAllowedOriginPatterns(allowedOriginPatterns);
    corsConfiguration.setAllowedMethods(List.of("*"));
    corsConfiguration.setAllowedHeaders(List.of("*"));
    corsConfiguration.addExposedHeader("Content-Disposition");
    corsConfiguration.addExposedHeader("X-Security-Event");
    corsConfiguration.setAllowCredentials(true);
    corsConfiguration.setMaxAge(maxAge);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }
}
