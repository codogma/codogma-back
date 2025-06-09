package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Palette")
@JsonInclude(Include.NON_NULL)
public class PaletteDTO {

  private SwatchDTO vibrant;
  private SwatchDTO muted;
  private SwatchDTO darkVibrant;
  private SwatchDTO darkMuted;
  private SwatchDTO lightVibrant;
  private SwatchDTO lightMuted;
}