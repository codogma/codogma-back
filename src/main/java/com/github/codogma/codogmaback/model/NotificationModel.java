package com.github.codogma.codogmaback.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Data
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class NotificationModel {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String recipient;
  private Long articleId;
  private Long commentId;
  @Column(nullable = false)
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @CollectionTable(name = "notification_localized_titles", joinColumns = @JoinColumn(name = "notification_id"))
  private Map<Language, String> title = new HashMap<>();
  @Column(nullable = false, columnDefinition = "TEXT")
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @CollectionTable(name = "notification_localized_messages", joinColumns = @JoinColumn(name = "notification_id"))
  private Map<Language, String> message = new HashMap<>();
  @Column(nullable = false)
  private boolean isRead;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
