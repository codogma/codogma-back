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
@Schema(name = "Swatch")
@JsonInclude(Include.NON_NULL)
public class SwatchDTO {

  private int r;
  private int g;
  private int b;
  private int population;
  private double h;
  private double s;
  private double l;
  private String hex;
  private String titleTextColor;
  private String bodyTextColor;
}