package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Compilation")
public class UpdateCompilation {

  @Schema(description = "Title of the compilation", example = "Java Basics")
  private String title;
  @Schema(description = "Compilation's image")
  private MultipartFile image;
  @Schema(description = "Description of the compilation", example = "About the basics of java programming")
  private String description;
}
