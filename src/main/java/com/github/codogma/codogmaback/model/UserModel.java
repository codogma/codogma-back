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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@ToString
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")})
public class UserModel implements UserDetails {

  @Id
  @GenericField
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true, nullable = false, updatable = false)
  private UUID uuid;
  @Column(unique = true)
  private Integer githubId;
  @Column(unique = true)
  private Integer gitlabId;
  @FullTextField
  @Column(unique = true, nullable = false)
  private String username;
  @Column(unique = true, nullable = false)
  private String email;
  @Column(nullable = false)
  private String password;
  @FullTextField
  private String firstName;
  @FullTextField
  private String lastName;
  @FullTextField
  private String shortInfo;
  @FullTextField
  @Column(columnDefinition = "TEXT")
  private String bio;
  private String avatarUrl;
  @Column(nullable = false)
  private boolean enabled = false;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Exclude
  private List<ArticleModel> articles;
  @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Exclude
  private List<SubscriptionModel> subscriptions = new ArrayList<>();
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Exclude
  private List<SubscriptionModel> subscribers = new ArrayList<>();
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Exclude
  private List<CompilationModel> compilations = new ArrayList<>();
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Exclude
  private List<FavoriteModel> favorites = new ArrayList<>();
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Exclude
  private List<BookmarkModel> bookmarks = new ArrayList<>();
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  @Exclude
  private List<CommentModel> comments = new ArrayList<>();
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void updateFrom(UserModel other) {
    if (other.getGitlabId() != null) {
      this.gitlabId = other.getGitlabId();
    }
    if (other.getGithubId() != null) {
      this.githubId = other.getGithubId();
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
