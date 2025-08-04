package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.handler.oauth.OAuth2ProviderHandler;
import com.github.codogma.codogmaback.model.RefreshTokenModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.RefreshTokenRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.security.JwtProvider;
import com.github.codogma.codogmaback.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  @Value("${spring.security.jwt.access-expiration}")
  private long accessExpiration;
  @Value("${spring.security.jwt.refresh-expiration}")
  private long refreshExpiration;

  private final CookieUtils cookieUtils;
  private final DeviceAwareService deviceAwareService;
  private final JwtProvider jwtProvider;
  private final List<OAuth2ProviderHandler> providerHandlers;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  private static final int MAX_SESSIONS_PER_USER = 5;

  @Transactional
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

    OAuth2ProviderHandler handler = providerHandlers.stream()
        .filter(h -> h.supports(registrationId)).findFirst()
        .orElseThrow(() -> new OAuth2AuthenticationException("Unknown provider"));

    UserModel user = handler.processOAuth2User(oAuth2User);
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

    UserModel existingUser = userRepository.findByEmail(user.getEmail()).orElseGet(() -> {
      user.setUuid(UUID.randomUUID());
      UserModel newUser = userRepository.save(user);
      newUser.setUsername("username_" + newUser.getUuid());
      return newUser;
    });
    existingUser.setEnabled(true);
    existingUser.updateFrom(user);
    userRepository.save(existingUser);

    HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getRequest();
    String deviceId = deviceAwareService.generateDeviceId(request);
    String accessToken = jwtProvider.generateAccessToken(existingUser, deviceId);
    String refreshToken = jwtProvider.generateRefreshToken(existingUser, deviceId);

    // Set tokens expiration
    Instant refreshTokenExpiry = Instant.now().plusSeconds(refreshExpiration);

    // Store refresh token
    RefreshTokenModel refreshTokenModel = RefreshTokenModel.builder()
        .tokenHash(jwtProvider.hashToken(refreshToken)).user(existingUser)
        .deviceId("oauth2-" + registrationId).expiresAt(refreshTokenExpiry)
        .revoked(false).build();
    refreshTokenRepository.save(refreshTokenModel);

    // Set cookies
    HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getResponse();
    if (response != null) {
      cookieUtils.setAccessTokenToHttpOnlyCookie(response, accessToken);
      cookieUtils.setRefreshTokenToHttpOnlyCookie(response, refreshToken);

      // Enforce session limits
      int activeSessions = refreshTokenRepository.countByUserId(existingUser.getId());
      if (activeSessions > MAX_SESSIONS_PER_USER) {
        refreshTokenRepository.findFirstByUserIdOrderByCreatedAtAsc(existingUser.getId())
            .ifPresent(refreshTokenRepository::delete);
      }
    }

    log.info("User {} authenticated with provider {}", existingUser.getUsername(), registrationId);
    return oAuth2User;
  }
}