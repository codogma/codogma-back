package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
@Schema(name = "Authentication Request")
public class SignInRequestDTO {

  @NotBlank(message = "Username or Email cannot be empty")
  @Size(min = 3, max = 50, message = "Username or Email must be between 3 and 50 characters")
  @Schema(description = "Username or Email of the user", example = "JohnDoe or test@test.com", requiredMode = RequiredMode.REQUIRED)
  private String usernameOrEmail;
  @NotBlank(message = "Password cannot be empty")
  @Schema(description = "Password", example = "P@ssword", requiredMode = RequiredMode.REQUIRED)
  @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
  private String password;
}