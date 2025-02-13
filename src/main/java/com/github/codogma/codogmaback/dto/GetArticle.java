package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Status of the article")
  private Status status;
  @Schema(description = "Language of the article")
  private Language language;
  @Schema(description = "Count of likes of the article", example = "10")
  private Integer likeCount;
  @Schema(description = "Original article")
  private GetArticle originalArticle;
  @Schema(description = "Check if the article is liked")
  private Boolean isLiked;
  @Schema(description = "Check to see if the article is in the compilation")
  private Boolean isCompilated;
  @Schema(description = "Count of compilations of the article", example = "10")
  private Integer compilationsCount;
  @Schema(description = "Count of comments of the article", example = "10")
  private Integer commentsCount = 0;
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
  @Schema(description = "Tags associated with the article", example = "java, programming, jvm")
  private List<GetTag> tags;
  @Schema(description = "Categories associated with the article")
  private List<GetCategory> categories;
  @Schema(description = "Compilations associated with the article")
  private List<GetCompilation> compilations;
  @Schema(description = "Date and time of the article creation", example = "2022-01-01T00:00:00.000Z")
  private Date createdAt;
  @Schema(description = "Date and time of the article update", example = "2022-01-01T01:00:00.000Z")
  private Date updatedAt;
}
