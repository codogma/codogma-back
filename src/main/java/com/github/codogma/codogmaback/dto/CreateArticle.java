package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Article")
public class CreateArticle {

  @NotNull(message = "Language cannot be empty")
  @Schema(description = "Language of the article", requiredMode = RequiredMode.REQUIRED)
  private Language language;
  @Schema(description = "ID of the article", example = "1")
  private Long originalArticleId;
  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the article", example = "My First Blog Article", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotNull(message = "Article's preview image cannot be empty")
  @Schema(description = "Article's preview image", requiredMode = RequiredMode.REQUIRED)
  private String imageUrl;
  @NotNull(message = "Preview content cannot be empty")
  @Schema(description = "Preview content of the article", example = "This is the preview content of the article", requiredMode = RequiredMode.REQUIRED)
  private String previewContent;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the article", example = "This is the content of the article", requiredMode = RequiredMode.REQUIRED)
  private String content;
  @NotNull(message = "Categories cannot be empty")
  @Schema(description = "Categories associated with the article")
  private List<Long> categoryIds;
  @Schema(description = "Tags associated with the article", requiredMode = RequiredMode.NOT_REQUIRED)
  private List<String> tags = new ArrayList<>();
  @Schema(type = "array", format = "binary", description = "Article images", requiredMode = RequiredMode.NOT_REQUIRED)
  private List<MultipartFile> images = new ArrayList<>();
}
