package com.github.codogma.codogmaback.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Data
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilations")
public class CompilationModel {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  @FullTextField(name = "title", analyzer = "standard")
  private String title;
  @FullTextField
  private String description;
  @Column(name = "image_url")
  private String imageUrl;
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "compilations")
  private List<ArticleModel> articles = new ArrayList<>();
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;
  @OneToMany(mappedBy = "compilation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BookmarkModel> bookmarks = new ArrayList<>();
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
