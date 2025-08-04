package com.github.codogma.codogmaback.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "article_images")
public class ArticleImageModel extends BaseImageModel {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id")
  private ArticleModel article;
  private boolean isPreview;
  @Embedded
  private Palette palette;
}