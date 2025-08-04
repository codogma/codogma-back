package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@Schema(name = "Get Category")
@JsonInclude(Include.NON_NULL)
public class GetCategory {

  @Schema(description = "ID of the category", example = "1")
  private Long id;
  @Schema(description = "Check if the category is favorite")
  private Boolean isFavorite;
  @Schema(description = "Name of the category", example = "Technology")
  private String name;
  @Schema(description = "Description of the category", example = "Category about technology")
  private String description;
  @Schema(description = "Icon of the category")
  private GetImage icon;
  @Schema(description = "Image of the category")
  private GetImageWithPalette image;
  @Schema(description = "Tags associated with the category articles")
  private List<GetTag> tags;
}