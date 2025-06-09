package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Article")
public class UpdateArticle {

  @NotNull(message = "Language cannot be null")
  @Schema(description = "Language of the article", example = "en", requiredMode = RequiredMode.REQUIRED)
  private Language language;
  @Schema(description = "ID of the article", example = "1")
  private Long originalArticleId;
  @NotBlank(message = "Title cannot be null and must contain a value")
  @Schema(description = "Title of the article", example = "My First Blog Article", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotBlank(message = "Preview content cannot be null and must contain a value")
  @Schema(description = "Preview content of the article", example = "This is the preview content of the article", requiredMode = RequiredMode.REQUIRED)
  private String previewContent;
  @NotBlank(message = "Content cannot be null and must contain a value")
  @Schema(description = "Content of the article", example = "This is the content of the article", requiredMode = RequiredMode.REQUIRED)
  private String content;
  @NotEmpty(message = "Categories cannot be null or empty")
  @Schema(description = "Categories associated with the article", requiredMode = RequiredMode.REQUIRED)
  private List<Long> categoryIds;
  @Schema(description = "Compilations associated with the article")
  private List<Long> compilationIds;
  @NotEmpty(message = "Tags cannot be null or empty")
  @Schema(description = "Tags associated with the article", requiredMode = RequiredMode.REQUIRED)
  private List<String> tags;
}
