package com.github.codogma.codogmaback.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@RequiredArgsConstructor
public class Palette {

  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "vibrant_r")),
      @AttributeOverride(name = "g", column = @Column(name = "vibrant_g")),
      @AttributeOverride(name = "b", column = @Column(name = "vibrant_b")),
      @AttributeOverride(name = "h", column = @Column(name = "vibrant_h")),
      @AttributeOverride(name = "s", column = @Column(name = "vibrant_s")),
      @AttributeOverride(name = "l", column = @Column(name = "vibrant_l")),
      @AttributeOverride(name = "population", column = @Column(name = "vibrant_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "vibrant_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "vibrant_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "vibrant_body_text_color"))})
  private Swatch vibrant;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "muted_r")),
      @AttributeOverride(name = "g", column = @Column(name = "muted_g")),
      @AttributeOverride(name = "b", column = @Column(name = "muted_b")),
      @AttributeOverride(name = "h", column = @Column(name = "muted_h")),
      @AttributeOverride(name = "s", column = @Column(name = "muted_s")),
      @AttributeOverride(name = "l", column = @Column(name = "muted_l")),
      @AttributeOverride(name = "population", column = @Column(name = "muted_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "muted_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "muted_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "muted_body_text_color"))})
  private Swatch muted;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "dark_vibrant_r")),
      @AttributeOverride(name = "g", column = @Column(name = "dark_vibrant_g")),
      @AttributeOverride(name = "b", column = @Column(name = "dark_vibrant_b")),
      @AttributeOverride(name = "h", column = @Column(name = "dark_vibrant_h")),
      @AttributeOverride(name = "s", column = @Column(name = "dark_vibrant_s")),
      @AttributeOverride(name = "l", column = @Column(name = "dark_vibrant_l")),
      @AttributeOverride(name = "population", column = @Column(name = "dark_vibrant_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "dark_vibrant_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "dark_vibrant_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "dark_vibrant_body_text_color"))})
  private Swatch darkVibrant;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "dark_muted_r")),
      @AttributeOverride(name = "g", column = @Column(name = "dark_muted_g")),
      @AttributeOverride(name = "b", column = @Column(name = "dark_muted_b")),
      @AttributeOverride(name = "h", column = @Column(name = "dark_muted_h")),
      @AttributeOverride(name = "s", column = @Column(name = "dark_muted_s")),
      @AttributeOverride(name = "l", column = @Column(name = "dark_muted_l")),
      @AttributeOverride(name = "population", column = @Column(name = "dark_muted_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "dark_muted_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "dark_muted_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "dark_muted_body_text_color"))})
  private Swatch darkMuted;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "light_vibrant_r")),
      @AttributeOverride(name = "g", column = @Column(name = "light_vibrant_g")),
      @AttributeOverride(name = "b", column = @Column(name = "light_vibrant_b")),
      @AttributeOverride(name = "h", column = @Column(name = "light_vibrant_h")),
      @AttributeOverride(name = "s", column = @Column(name = "light_vibrant_s")),
      @AttributeOverride(name = "l", column = @Column(name = "light_vibrant_l")),
      @AttributeOverride(name = "population", column = @Column(name = "light_vibrant_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "light_vibrant_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "light_vibrant_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "light_vibrant_body_text_color"))})
  private Swatch lightVibrant;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "r", column = @Column(name = "light_muted_r")),
      @AttributeOverride(name = "g", column = @Column(name = "light_muted_g")),
      @AttributeOverride(name = "b", column = @Column(name = "light_muted_b")),
      @AttributeOverride(name = "h", column = @Column(name = "light_muted_h")),
      @AttributeOverride(name = "s", column = @Column(name = "light_muted_s")),
      @AttributeOverride(name = "l", column = @Column(name = "light_muted_l")),
      @AttributeOverride(name = "population", column = @Column(name = "light_muted_population")),
      @AttributeOverride(name = "hex", column = @Column(name = "light_muted_hex")),
      @AttributeOverride(name = "titleTextColor", column = @Column(name = "light_muted_title_text_color")),
      @AttributeOverride(name = "bodyTextColor", column = @Column(name = "light_muted_body_text_color"))})
  private Swatch lightMuted;
}