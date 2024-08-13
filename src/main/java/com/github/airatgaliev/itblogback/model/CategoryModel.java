package com.github.airatgaliev.itblogback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  private String description;

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
  private List<ArticleModel> articles = new ArrayList<>();

//  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
//  private List<TagModel> tags = new ArrayList<>();
}