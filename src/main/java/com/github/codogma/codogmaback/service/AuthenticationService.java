package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.AuthDTO;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.SignInRequest;
import com.github.codogma.codogmaback.dto.SignUpRequest;
import com.github.codogma.codogmaback.exception.DeviceMismatchException;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.exception.InvalidTokenException;
import com.github.codogma.codogmaback.exception.RevokedTokenException;
import com.github.codogma.codogmaback.exception.TokenExpiredException;
import com.github.codogma.codogmaback.model.ConfirmationToken;
import com.github.codogma.codogmaback.model.RefreshTokenModel;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.RefreshTokenRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.security.JwtProvider;
import com.github.codogma.codogmaback.util.CookieUtils;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

  @Value("${spring.security.jwt.device-claim-name}")
  private String deviceClaimName;
  @Value("${spring.security.jwt.access-expiration}")
  private long accessExpiration;
  @Value("${spring.security.jwt.refresh-expiration}")
  private long refreshExpiration;

  private final AuthenticationManager authenticationManager;
  private final ConfirmationTokenService tokenService;
  private final CookieUtils cookieUtils;
  private final DeviceAwareService deviceAwareService;
  private final EmailService emailService;
  private final ExceptionFactory exceptionFactory;
  private final JwtProvider jwtProvider;
  private final FileUploadUtil fileUploadUtil;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenRevocationService tokenRevocationService;
  private final UserRepository userRepository;

  private static final int MAX_SESSIONS_PER_USER = 5;

  @Transactional
  public GetUser signUp(SignUpRequest signUpRequest, MultipartFile avatar,
      HttpServletRequest request) {
    userRepository.findByUsernameOrEmail(signUpRequest.getUsername(), signUpRequest.getEmail())
        .ifPresent(user -> {
          throw exceptionFactory.userAlreadyExists();
        });
    String origin = request.getHeader("Origin");
    UserModel user = UserModel.builder().uuid(UUID.randomUUID())
        .username(signUpRequest.getUsername()).email(signUpRequest.getEmail())
        .password(passwordEncoder.encode(signUpRequest.getPassword())).role(Role.ROLE_USER).build();
    Optional.ofNullable(avatar).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryImage).ifPresent(user::setAvatarUrl);
    userRepository.save(user);
    String token = UUID.randomUUID().toString();
    ConfirmationToken confirmationToken = ConfirmationToken.builder().token(token).user(user)
        .createdAt(Instant.now()).expiresAt(Instant.now().plusSeconds(24 * 3600L)).build();
    tokenService.saveConfirmationToken(confirmationToken);
    emailService.sendEmailVerification(user.getEmail(), token, origin);
    return convertUserModelToCetUserDTO(user);
  }

  @Transactional
  public void confirmEmail(String token) {
    ConfirmationToken confirmationToken = tokenService.getToken(token)
        .orElseThrow(exceptionFactory::invalidToken);
    if (confirmationToken.getConfirmedAt() != null) {
      throw exceptionFactory.emailAlreadyConfirmed();
    }
    if (confirmationToken.getExpiresAt().isBefore(Instant.now())) {
      throw exceptionFactory.tokenExpired();
    }
    UserModel user = confirmationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);
    tokenService.setConfirmedAt(token);
    log.info("Email confirmed for user: {}", user.getUsername());
  }

  @Transactional
  public AuthDTO signIn(SignInRequest input, HttpServletRequest request,
      HttpServletResponse response) {
    log.info("Attempting to authenticate user: {}", input.getUsernameOrEmail());
    try {
      UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
          input.getUsernameOrEmail()).orElseThrow(exceptionFactory::usernameOrEmailNotFound);
      if (!user.isEnabled()) {
        throw exceptionFactory.emailNotConfirmed();
      }
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));

      // Generate tokens
      String deviceId = deviceAwareService.generateDeviceId(request);
      String accessToken = jwtProvider.generateAccessToken(user, deviceId);
      String refreshToken = jwtProvider.generateRefreshToken(user, deviceId);
      refreshTokenRepository.deleteByUserUsernameAndDeviceId(user.getUsername(), deviceId);

      // Set tokens expiration
      Instant accessTokenExpiry = Instant.now().plusSeconds(accessExpiration);
      Instant refreshTokenExpiry = Instant.now().plusSeconds(refreshExpiration);

      // Store refresh token
      RefreshTokenModel refreshTokenModel = RefreshTokenModel.builder()
          .tokenHash(jwtProvider.hashToken(refreshToken)).user(user).deviceId(deviceId)
          .expiresAt(refreshTokenExpiry).revoked(false).build();
      refreshTokenRepository.save(refreshTokenModel);

      // Set cookies
      cookieUtils.setAccessTokenToHttpOnlyCookie(response, accessToken);
      cookieUtils.setRefreshTokenToHttpOnlyCookie(response, refreshToken);

      // Enforce session limits
      int activeSessions = refreshTokenRepository.countByUserId(user.getId());
      if (activeSessions > MAX_SESSIONS_PER_USER) {
        refreshTokenRepository.findFirstByUserIdOrderByCreatedAtAsc(user.getId())
            .ifPresent(refreshTokenRepository::delete);
      }

      return convertUserModelToAuthDTO(user, accessTokenExpiry);
    } catch (Exception e) {
      log.error("Authentication failed for user: {}", input.getUsernameOrEmail(), e);
      throw e;
    }
  }

  @Transactional
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshTokenStr = cookieUtils.extractRefreshToken(request);
    if (refreshTokenStr == null) {
      cookieUtils.invalidateAllTokens(response);
      throw new InvalidTokenException("Refresh token not found");
    }

    try {
      Claims claims = jwtProvider.extractAllClaims(refreshTokenStr);
      String jti = claims.getId();
      if (tokenRevocationService.isTokenRevoked(jti)) {
        throw new RevokedTokenException("Refresh token revoked");
      }

      String username = claims.getSubject();
      UserModel user = userRepository.findByUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));

      String tokenDeviceId = claims.get(deviceClaimName, String.class);
      String currentDeviceId = deviceAwareService.generateDeviceId(request);
      if (!tokenDeviceId.equals(currentDeviceId)) {
        throw new DeviceMismatchException("Device mismatch");
      }

      String newAccessToken = jwtProvider.generateAccessToken(user, currentDeviceId);
      String newRefreshTokenStr = jwtProvider.generateRefreshToken(user, currentDeviceId);
      String newRefreshTokenHash = jwtProvider.hashToken(newRefreshTokenStr);

      List<RefreshTokenModel> existingTokens = refreshTokenRepository.findAllByUserUsernameAndDeviceId(
          username, currentDeviceId);

      // Set tokens expiration
      Instant refreshTokenExpiry = Instant.now().plusSeconds(refreshExpiration);

      if (!existingTokens.isEmpty()) {
        if (existingTokens.size() > 1) {
          log.warn("Found {} duplicate refresh tokens for user {} and device {}. Cleaning up.",
              existingTokens.size(), username, currentDeviceId);
          refreshTokenRepository.deleteAll(existingTokens);
        } else {
          RefreshTokenModel existingToken = existingTokens.getFirst();
          existingToken.setTokenHash(newRefreshTokenHash);
          existingToken.setExpiresAt(refreshTokenExpiry);
          existingToken.setRevoked(false);
          refreshTokenRepository.save(existingToken);
        }
      } else {
        RefreshTokenModel newStoredToken = RefreshTokenModel.builder()
            .tokenHash(newRefreshTokenHash).user(user).deviceId(currentDeviceId)
            .expiresAt(refreshTokenExpiry).revoked(false).build();
        refreshTokenRepository.save(newStoredToken);
      }

      cookieUtils.setAccessTokenToHttpOnlyCookie(response, newAccessToken);
      cookieUtils.setRefreshTokenToHttpOnlyCookie(response, newRefreshTokenStr);

    } catch (ExpiredJwtException e) {
      cookieUtils.invalidateAllTokens(response);
      throw new TokenExpiredException("Refresh token expired");
    }
  }

  public AuthDTO currentUser(HttpServletRequest request, HttpServletResponse response) {
    String accessTokenStr = cookieUtils.extractAccessToken(request);
    String refreshTokenStr = cookieUtils.extractRefreshToken(request);

    if (refreshTokenStr == null) {
      cookieUtils.invalidateAllTokens(response);
      throw new InvalidTokenException("Refresh token not found");
    }

    try {
      Claims refreshClaims = jwtProvider.extractAllClaims(refreshTokenStr);
      String jti = refreshClaims.getId();
      if (tokenRevocationService.isTokenRevoked(jti)) {
        throw new RevokedTokenException("Refresh token revoked");
      }

      String username = refreshClaims.getSubject();
      UserModel user = userRepository.findByUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));

      String tokenDeviceId = refreshClaims.get(deviceClaimName, String.class);
      String currentDeviceId = deviceAwareService.generateDeviceId(request);
      if (!tokenDeviceId.equals(currentDeviceId)) {
        throw new DeviceMismatchException("Device mismatch");
      }

      boolean needsRefresh = false;
      Instant accessTokenExpiry = Instant.now().plusSeconds(accessExpiration);
      if (accessTokenStr == null) {
        log.info("Access token missing for user: {}, refreshing...", username);
        needsRefresh = true;
      } else {
        try {
          Claims accessClaims = jwtProvider.extractAllClaims(accessTokenStr);
          accessTokenExpiry = accessClaims.getExpiration().toInstant();
          Instant now = Instant.now();

          long secondsLeft = Duration.between(now, accessTokenExpiry).getSeconds();
          if (secondsLeft < 300) {
            log.info("Access token expires in {} seconds for user: {}, refreshing...", secondsLeft,
                username);
            needsRefresh = true;
          }

        } catch (ExpiredJwtException e) {
          // Access token истек, будем обновлять
          log.info("Access token expired for user: {}, refreshing...", username);
          needsRefresh = true;
        }
      }

      if (needsRefresh) {
        String newAccessToken = jwtProvider.generateAccessToken(user, currentDeviceId);
        String newRefreshTokenStr = jwtProvider.generateRefreshToken(user, currentDeviceId);
        String newRefreshTokenHash = jwtProvider.hashToken(newRefreshTokenStr);

        List<RefreshTokenModel> existingTokens = refreshTokenRepository.findAllByUserUsernameAndDeviceId(
            username, currentDeviceId);

        // Set tokens expiration
        Instant refreshTokenExpiry = Instant.now().plusSeconds(refreshExpiration);

        if (!existingTokens.isEmpty()) {
          if (existingTokens.size() > 1) {
            log.warn("Found {} duplicate refresh tokens for user {} and device {}. Cleaning up.",
                existingTokens.size(), username, currentDeviceId);
            refreshTokenRepository.deleteAll(existingTokens);
          } else {
            RefreshTokenModel existingToken = existingTokens.getFirst();
            existingToken.setTokenHash(newRefreshTokenHash);
            existingToken.setExpiresAt(refreshTokenExpiry);
            existingToken.setRevoked(false);
            refreshTokenRepository.save(existingToken);
          }
        } else {
          RefreshTokenModel newStoredToken = RefreshTokenModel.builder()
              .tokenHash(newRefreshTokenHash).user(user).deviceId(currentDeviceId)
              .expiresAt(refreshTokenExpiry).revoked(false).build();
          refreshTokenRepository.save(newStoredToken);
        }

        cookieUtils.setAccessTokenToHttpOnlyCookie(response, newAccessToken);
        cookieUtils.setRefreshTokenToHttpOnlyCookie(response, newRefreshTokenStr);
      }

      return convertUserModelToAuthDTO(user, accessTokenExpiry);
    } catch (ExpiredJwtException e) {
      cookieUtils.invalidateAllTokens(response);
      throw new TokenExpiredException("Refresh token expired");
    }
  }

  @Transactional
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieUtils.extractRefreshToken(request);
    log.info("Revoking refresh token: {}", refreshToken);
    String username = jwtProvider.extractUsername(refreshToken);
    log.info("Attempting to logout user: {}", username);

    if (refreshToken != null) {
      Claims claims = jwtProvider.extractAllClaims(refreshToken);
      String jti = claims.getId();
      tokenRevocationService.revokeToken(jti);
    }

    cookieUtils.invalidateAllTokens(response);
    String deviceId = deviceAwareService.generateDeviceId(request);
    refreshTokenRepository.deleteByUserUsernameAndDeviceId(username, deviceId);
    log.info("User logged out successfully: {}", username);
  }

  private GetUser convertUserModelToCetUserDTO(UserModel userModel) {
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .bio(userModel.getBio()).role(userModel.getRole()).avatarUrl(userModel.getAvatarUrl())
        .build();
  }

  private AuthDTO convertUserModelToAuthDTO(UserModel user, Instant expiresAt) {
    return AuthDTO.builder().id(user.getUuid()).name(user.getUsername()).email(user.getEmail())
        .image(user.getAvatarUrl()).role(user.getRole()).expires(expiresAt).build();
  }
}