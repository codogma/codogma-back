package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Comment", description = "Update the content of a comment")
public class UpdateCommentDTO {

  @NotBlank(message = "Status cannot be null and must contain a value")
  @Schema(description = "Content of the comment", example = "This is the updated content of the comment", minLength = 2, maxLength = 1000, requiredMode = RequiredMode.REQUIRED)
  private String content;
}