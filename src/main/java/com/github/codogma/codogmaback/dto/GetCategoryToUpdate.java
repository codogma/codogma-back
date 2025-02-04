package com.github.codogma.codogmaback.dto;

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
public class GetCategoryToUpdate {

  @Schema(description = "Name of the category", example = "{ \"en\": \"Programming\", \"ru\": \"Программирование\" }")
  private Map<Language, String> name;
  @Schema(description = "Image of the category")
  private String imageUrl;
  @Schema(description = "Description of the category", example = "{ \"en\": \"Articles about technology\", \"ru\": \"Статьи о технологиях\" }")
  private Map<Language, String> description;
}