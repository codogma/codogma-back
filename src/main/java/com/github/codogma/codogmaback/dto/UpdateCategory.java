package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Category")
public class UpdateCategory {

  @Schema(description = "Name of the category", example = "{ \"en\": \"Programming\", \"ru\": \"Программирование\" }")
  private Map<Language, String> name;
  @Schema(description = "Category's icon")
  private MultipartFile icon;
  @Schema(description = "Category's image")
  private MultipartFile image;
  @Schema(description = "Palette of the image")
  private PaletteDTO palette;
  @Schema(description = "Description of the category", example = "{ \"en\": \"Articles about technology\", \"ru\": \"Статьи о технологиях\" }")
  private Map<Language, String> description;
}