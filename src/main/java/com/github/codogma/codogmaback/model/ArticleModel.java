package com.github.codogma.codogmaback.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.AlternativeDiscriminator;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

@Getter
@Setter
@ToString
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "articles")
public class ArticleModel {

  @Id
  @GenericField
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @FullTextField
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status = Status.DRAFT;
  @FullTextField
  @AlternativeDiscriminator
  @Enumerated(EnumType.STRING)
  private Language language;
  @Column(nullable = false)
  @GenericField(sortable = Sortable.YES)
  private Integer likeCount;
  private Long originalArticleId;
  @FullTextField
  @MultiLanguageField
  @Column(nullable = false)
  private String title;
  @Builder.Default
  @ToString.Exclude
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ArticleImageModel> images = new ArrayList<>();
  @FullTextField
  @Column(columnDefinition = "TEXT")
  private String previewContent;
  @FullTextField
  @MultiLanguageField
  @Column(columnDefinition = "TEXT")
  private String content;
  @ToString.Exclude
  @IndexedEmbedded
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;
  @ToString.Exclude
  @Builder.Default
  @IndexedEmbedded(includePaths = "id")
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_categories", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
  private List<CategoryModel> categories = new ArrayList<>();
  @ToString.Exclude
  @Builder.Default
  @IndexedEmbedded
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CompilationArticle> compilationArticles = new ArrayList<>();
  @ToString.Exclude
  @Builder.Default
  @IndexedEmbedded(includePaths = {"id", "name"})
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<TagModel> tags = new ArrayList<>();
  @ToString.Exclude
  @OrderBy("createdAt ASC")
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CommentModel> comments = new ArrayList<>();
  @ToString.Exclude
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ArticleView> views = new ArrayList<>();
  @CreationTimestamp
  @GenericField(sortable = Sortable.YES)
  @Column(nullable = false, updatable = false, name = "created_at")
  private Instant createdAt;
  @GenericField(sortable = Sortable.YES)
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}