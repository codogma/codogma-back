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
@Schema(name = "Create Compilation")
public class CreateCompilation {

  @Schema(description = "Title of the compilation", example = "Java Basics", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @Schema(description = "Compilation's image")
  private MultipartFile image;
  @Schema(description = "Description of the compilation", example = "About the basics of java programming")
  private String description;
}
