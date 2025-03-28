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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
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
  @Column(name = "image_url")
  private String imageUrl;
  @FullTextField
  @Column(columnDefinition = "TEXT")
  private String previewContent;
  @FullTextField
  @MultiLanguageField
  @Column(columnDefinition = "TEXT")
  private String content;
  @IndexedEmbedded
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @Exclude
  private UserModel user;
  @Default
  @IndexedEmbedded(includePaths = {"id"})
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_categories", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
  @Exclude
  private List<CategoryModel> categories = new ArrayList<>();
  @Builder.Default
  @IndexedEmbedded
  @Exclude
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "article_compilations", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "compilation_id"), uniqueConstraints = {
      @UniqueConstraint(columnNames = {"article_id", "compilation_id"})})
  private List<CompilationModel> compilations = new ArrayList<>();
  @Default
  @IndexedEmbedded(includePaths = {"id", "name"})
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Exclude
  private List<TagModel> tags = new ArrayList<>();
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  @Exclude
  private List<CommentModel> comments = new ArrayList<>();
  @GenericField(sortable = Sortable.YES)
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;
  @GenericField(sortable = Sortable.YES)
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}