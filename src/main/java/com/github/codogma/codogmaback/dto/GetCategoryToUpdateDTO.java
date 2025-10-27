package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Category to update")
@JsonInclude(Include.NON_NULL)
public class GetCategoryToUpdateDTO {

  @Schema(description = "Name of the category", example = "{ \"en\": \"Programming\", \"ru\": \"Программирование\" }")
  private Map<Language, String> name;
  @Schema(description = "Icon of the category")
  private GetImageDTO icon;
  @Schema(description = "Image of the category")
  private GetImageWithPalette image;
  @Schema(description = "Description of the category", example = "{ \"en\": \"Articles about technology\", \"ru\": \"Статьи о технологиях\" }")
  private Map<Language, String> description;
}