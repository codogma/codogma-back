package com.github.codogma.codogmaback.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Getter
@Setter
@ToString
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class CategoryModel {

  @Id
  @GenericField
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @FullTextField(name = "name", analyzer = "standard")
  @CollectionTable(name = "category_localized_names", joinColumns = @JoinColumn(name = "category_id"), uniqueConstraints = {
      @UniqueConstraint(columnNames = {"name", "language"})})
  @Column(nullable = false)
  private Map<Language, String> name = new HashMap<>();
  @FullTextField
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @CollectionTable(name = "category_localized_descriptions", joinColumns = @JoinColumn(name = "category_id"))
  private Map<Language, String> description = new HashMap<>();
  @Default
  @Exclude
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CategoryImageModel> images = new ArrayList<>();
  @Default
  @Exclude
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
  private List<ArticleModel> articles = new ArrayList<>();
  @Default
  @Exclude
  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FavoriteModel> favorites = new ArrayList<>();
  @CreationTimestamp
  @GenericField(sortable = Sortable.YES)
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  @GenericField(sortable = Sortable.YES)
  private LocalDateTime updatedAt;
}