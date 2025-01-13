package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Category")
public class CreateCategory {

  @NotNull(message = "Name cannot be empty")
  @Schema(description = "Name of the category", example = "Programming", requiredMode = RequiredMode.REQUIRED)
  private String name;
  @Schema(description = "Category's image", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile image;
  @Schema(description = "Description of the category", example = "Articles about technology")
  private String description;
}