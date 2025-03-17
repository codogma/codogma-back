package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.AuthenticationResponse;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.SignInRequest;
import com.github.codogma.codogmaback.dto.SignUpRequest;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.handler.oauth.OAuth2ProviderHandler;
import com.github.codogma.codogmaback.model.ConfirmationToken;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import static com.github.codogma.codogmaback.util.TokenUtils.invalidateToken;
import static com.github.codogma.codogmaback.util.TokenUtils.setAuthCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final FileUploadUtil fileUploadUtil;
  private final List<OAuth2ProviderHandler> providerHandlers;
  private final EmailService emailService;
  private final ConfirmationTokenService tokenService;
  private final ExceptionFactory exceptionFactory;

  @Transactional
  public GetUser signUp(SignUpRequest signUpRequest, MultipartFile avatar, String origin) {
    userRepository.findByUsernameOrEmail(signUpRequest.getUsername(), signUpRequest.getEmail())
        .ifPresent((user) -> {
          throw exceptionFactory.userAlreadyExists();
        });
    UserModel user = UserModel.builder().username(signUpRequest.getUsername())
        .email(signUpRequest.getEmail())
        .password(passwordEncoder.encode(signUpRequest.getPassword())).role(Role.ROLE_USER).build();
    Optional.ofNullable(avatar).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryAvatar).ifPresent(user::setAvatarUrl);
    userRepository.save(user);
    String token = jwtService.generateToken(user);
    ConfirmationToken confirmationToken = ConfirmationToken.builder().token(token).user(user)
        .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusHours(24)).build();
    tokenService.saveConfirmationToken(confirmationToken);
//    emailService.sendEmailVerification(user.getEmail(), token, origin);
    return convertUserModelToDto(user);
  }

  @Transactional
  public void confirmEmail(String token) {
    ConfirmationToken confirmationToken = tokenService.getToken(token)
        .orElseThrow(exceptionFactory::invalidToken);
    if (confirmationToken.getConfirmedAt() != null) {
      throw exceptionFactory.emailAlreadyConfirmed();
    }
    if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw exceptionFactory.tokenExpired();
    }
    UserModel user = confirmationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);
    tokenService.setConfirmedAt(token);
    log.info("Email confirmed for user: {}", user.getUsername());
  }

  @Transactional
  public AuthenticationResponse signIn(SignInRequest input, HttpServletResponse response) {
    log.info("Attempting to authenticate user: {}", input.getUsernameOrEmail());
    try {
      UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
          input.getUsernameOrEmail()).orElseThrow(exceptionFactory::usernameOrEmailNotFound);
      if (!user.isEnabled()) {
        throw exceptionFactory.emailNotConfirmed();
      }
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));
      String jwtToken = jwtService.generateToken(user);
      setAuthCookie(response, jwtToken);
      return AuthenticationResponse.builder().token(jwtToken)
          .expiresIn(jwtService.getExpirationTime()).build();
    } catch (Exception e) {
      log.error("Authentication failed for user: {}", input.getUsernameOrEmail(), e);
      throw e;
    }
  }

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
      UserModel newUser = userRepository.save(user);
      newUser.setUsername("username_" + newUser.getId());
      return newUser;
    });
    existingUser.setEnabled(true);
    existingUser.updateFrom(user);
    userRepository.save(existingUser);

    String jwtToken = jwtService.generateToken(existingUser);
    HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getResponse();
    if (response != null) {
      setAuthCookie(response, jwtToken);
    }
    log.info("User {} authenticated with provider {}", existingUser.getUsername(), registrationId);
    return oAuth2User;
  }

  public void refreshToken(HttpServletResponse response, UserModel userModel) {
    if (userModel != null) {
      String newToken = jwtService.generateToken(userModel);
      setAuthCookie(response, newToken);
    }
  }

  public Optional<GetUser> currentUser(UserModel userModel) {
    return Optional.ofNullable(userModel).map(this::convertUserModelToDto);
  }

  public void logout(HttpServletResponse response, UserModel userModel) {
    log.info("Attempting to logout user: {}", userModel.getUsername());
    invalidateToken(response);
    log.info("User logged out successfully: {}", userModel.getUsername());
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .bio(userModel.getBio()).role(userModel.getRole()).avatarUrl(userModel.getAvatarUrl())
        .build();
  }
}