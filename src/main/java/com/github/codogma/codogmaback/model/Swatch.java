package com.github.codogma.codogmaback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@RequiredArgsConstructor
public class Swatch {

  @Column(name = "rgb_r")
  private int r;
  @Column(name = "rgb_g")
  private int g;
  @Column(name = "rgb_b")
  private int b;

  private int population;

  @Column(name = "hsl_h")
  private double h;
  @Column(name = "hsl_s")
  private double s;
  @Column(name = "hsl_l")
  private double l;

  private String hex;
  private String titleTextColor;
  private String bodyTextColor;
}