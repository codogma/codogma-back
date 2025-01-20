package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.codogma.codogmaback.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Notification")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetNotification {

  @Schema(description = "ID of the notification", example = "1")
  private Long id;
  @Schema(description = "Article Id", example = "1")
  private Long articleId;
  @Schema(description = "Comment Id", example = "1")
  private Long commentId;
  @Schema(description = "Title of the notification", example = "Warning")
  private String title;
  @Schema(description = "Message of the notification", example = "This is a warning message")
  private String message;
  private NotificationType type;
  @Schema(description = "Check if the notification is read")
  private boolean isRead;
}
