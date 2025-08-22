package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.codogma.codogmaback.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Authentication Response")
@JsonInclude(Include.NON_NULL)
public class AuthDTO {

  @Schema(description = "User id", example = "00000000-0000-0000-0000-000000000000")
  private UUID id;
  @Schema(description = "User name", example = "John Doe")
  private String name;
  @Schema(description = "User email", example = "test@test.com")
  private String email;
  @Schema(description = "User avatar image path")
  private String image;
  @Schema(description = "User role", example = "ROLE_USER")
  private Role role;
  @Schema(description = "Token expiration time", example = "2022-01-01T00:00:00.000Z")
  private Instant expires;
}