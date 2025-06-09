package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create draft article")
public class CreateDraftArticle {

  @NotBlank(message = "Title cannot be null and must contain a value")
  @Schema(description = "Title of the article", example = "My First Blog Article", requiredMode = RequiredMode.REQUIRED)
  private String title;
}
