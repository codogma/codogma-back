package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update draft article")
public class UpdateDraftArticle {

  @Schema(description = "Language of the article")
  private Language language;
  @Schema(description = "ID of the article", example = "1")
  private Long originalArticleId;
  @Schema(description = "Title of the article", example = "My First Blog Article")
  private String title;
  @Schema(description = "Preview content of the article", example = "This is the preview content of the article")
  private String previewContent;
  @Schema(description = "Content of the article", example = "This is the content of the article")
  private String content;
  @Schema(description = "Categories associated with the article")
  private List<Long> categoryIds;
  @Schema(description = "Compilations associated with the article")
  private List<Long> compilationIds;
  @Schema(description = "Tags associated with the article")
  private List<String> tags;
}
