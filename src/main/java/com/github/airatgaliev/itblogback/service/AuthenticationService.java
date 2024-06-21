package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponseDTO;
import com.github.airatgaliev.itblogback.dto.SignInRequestDTO;
import com.github.airatgaliev.itblogback.dto.SignUpRequestDTO;
import com.github.airatgaliev.itblogback.dto.UserDTO;
import com.github.airatgaliev.itblogback.exception.UserAlreadyExistsException;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final AuthenticationManager authenticationManager;

  private final JwtService jwtService;

  public UserDTO signup(SignUpRequestDTO input) {
    new UserModel();
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new UserAlreadyExistsException("Email already exists: " + input.getEmail());
    }
    if (userRepository.existsByUsername(input.getUsername())) {
      throw new UserAlreadyExistsException("Username already exists: " + input.getUsername());
    }
    UserModel user = UserModel.builder().username(input.getUsername()).email(input.getEmail())
        .password(passwordEncoder.encode(input.getPassword())).role(Role.USER).build();

    UserModel savedUser = userRepository.save(user);
    return UserDTO.builder()
        .id(savedUser.getId())
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .firstName(savedUser.getFirstName())
        .lastName(savedUser.getLastName())
        .role(savedUser.getRole())
        .build();
  }

  public AuthenticationResponseDTO authenticate(SignInRequestDTO input) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));
    UserModel user = userRepository.findByEmail(input.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    String jwtToken = jwtService.generateToken(user);
    return AuthenticationResponseDTO.builder().token(jwtToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }
}
