package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Article Image")
public class CreateArticleImageDTO {

  @Schema(description = "Image file", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile image;
  @Schema(description = "Is preview image")
  private boolean isPreview;
  @Schema(description = "Palette of the image", type = "string", format = "binary")
  private PaletteDTO palette;
}