package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
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
@Schema(name = "Create Category")
public class CreateCategory {

  @NotNull(message = "Name cannot be empty")
  @Schema(description = "Name of the category", example = "{ \"en\": \"Programming\", \"ru\": \"Программирование\" }", requiredMode = RequiredMode.REQUIRED)
  private Map<Language, String> name;
  @Schema(description = "Category's image", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile image;
  @Schema(description = "Description of the category", example = "{ \"en\": \"Articles about technology\", \"ru\": \"Статьи о технологиях\" }")
  private Map<Language, String> description;
}