package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update User")
public class UpdateUserDTO {

  @Schema(description = "Username of the user", example = "JohnDoe")
  private String username;
  @Schema(description = "First name of the user", example = "John")
  private String firstName;
  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;
  @Schema(description = "Short info of the user", example = "I am a programmer")
  private String shortInfo;
  @Schema(description = "Bio of the user", example = "My name is John Doe, I am a programmer")
  private String bio;
  @Schema(description = "Email", example = "test@test.com")
  @Email(message = "Invalid email address")
  private String newEmail;
  @Schema(description = "User's current password", example = "P@ssword")
  private String currentPassword;
  @Schema(description = "User's new password", example = "P@ssw0rd")
  private String newPassword;
  @Schema(description = "User's avatar image")
  private MultipartFile avatar;
}
