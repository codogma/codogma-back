package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Compilation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetCompilation {

  @Schema(description = "ID of the compilation", example = "1")
  private Long id;
  @Schema(description = "Check if the compilation is bookmarked")
  private Boolean isBookmarked;
  @Schema(description = "Count of bookmarks of the compilation", example = "10")
  private Integer bookmarksCount;
  @Schema(description = "Title of the compilation", example = "Java Basics")
  private String title;
  @Schema(description = "Description of the compilation", example = "About the basics of java programming")
  private String description;
  @Schema(description = "Owner name of the compilation", example = "john")
  private String ownerName;
  @Schema(description = "Owner full name of the compilation", example = "John Doe")
  private String ownerFullName;
  @Schema(description = "Owner's avatar image")
  private String ownerAvatarUrl;
  @Schema(description = "Image of the compilation")
  private String imageUrl;
}
