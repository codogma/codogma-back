package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class UpdateCompilationDTO {

  @Schema(description = "Title of the compilation", example = "Java Basics")
  private String title;
  @Schema(description = "Compilation's image")
  private MultipartFile image;
  @Schema(description = "Description of the compilation", example = "About the basics of java programming")
  private String description;
  @Schema(description = "Ordered list of article IDs (duplicates will be rejected)")
  private List<Long> articleIds = new ArrayList<>();

  @AssertTrue(message = "Article IDs must be unique")
  private boolean isArticleIdsUnique() {
    return articleIds == null || new HashSet<>(articleIds).size() == articleIds.size();
  }
}