package com.github.airatgaliev.itblogback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Article")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetArticle {

  @Schema(description = "ID of the article", example = "1")
  private Long id;
  @Schema(description = "Title of the article", example = "My First Blog Article")
  private String title;
  @Schema(description = "Preview content of the article", example = "This is the preview content of the article")
  private String previewContent;
  @Schema(description = "Content of the article", example = "This is the content of the article")
  private String content;
  @Schema(description = "Username associated with the article", example = "JohnDoe")
  private String username;
  @Schema(description = "Author's avatar image")
  private String authorAvatarUrl;
  @Schema(description = "Tags associated with the article", requiredMode = RequiredMode.NOT_REQUIRED)
  private List<GetTag> tags;
  @Schema(description = "Categories associated with the article")
  private List<GetCategory> categories;
  @Schema(description = "Date and time of the article creation", example = "2022-01-01T00:00:00.000Z")
  private Date createdAt;
  @Schema(description = "Date and time of the article update", example = "2022-01-01T01:00:00.000Z")
  private Date updatedAt;
}
