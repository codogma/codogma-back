package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Notification")
public class CreateNotification {

  @Schema(description = "Recipient of the notification", example = "user")
  private String recipient;
  @Schema(description = "Entity Id of the notification", example = "1")
  private Long entityId;
  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the notification", example = "Warning", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotNull(message = "Message cannot be empty")
  @Schema(description = "Message of the notification", example = "This is a warning message", requiredMode = RequiredMode.REQUIRED)
  private String message;
  @Schema(description = "Notification type of the notification", example = "SYSTEM", requiredMode = RequiredMode.REQUIRED)
  private NotificationType type;
}
