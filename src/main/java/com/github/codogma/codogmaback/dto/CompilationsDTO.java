package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Compilations")
public class CompilationsDTO {

  @NotNull(message = "Compilation ids cannot be null")
  @Schema(description = "Compilation ids associated with the article")
  private List<Long> compilationIds;
}
