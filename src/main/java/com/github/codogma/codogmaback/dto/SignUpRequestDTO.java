package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Register User")
public class SignUpRequestDTO {

  @NotBlank(message = "Username cannot be empty")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  @Schema(description = "Username of the user", example = "JohnDoe", requiredMode = RequiredMode.REQUIRED)
  private String username;
  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email address")
  @Schema(description = "Email", example = "test@test.com", requiredMode = RequiredMode.REQUIRED)
  private String email;
  @NotBlank(message = "Password cannot be empty")
  @Schema(description = "Password", example = "P@ssword", requiredMode = RequiredMode.REQUIRED)
  @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
  private String password;
}