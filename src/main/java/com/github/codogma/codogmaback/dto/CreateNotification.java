package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
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

  @NotEmpty(message = "Title cannot be empty")
  @Schema(description = "Title of the notification", example = "{ \"en\": \"System Update\", \"ru\": \"Системное обновление\" }", requiredMode = RequiredMode.REQUIRED)
  private Map<Language, String> title;
  @NotEmpty(message = "Message cannot be empty")
  @Schema(description = "Message of the notification", example = "{ \"en\": \"The system will be updated at 2 AM.\", \"ru\": \"Система будет обновлена в 2 часа ночи.\" }", requiredMode = RequiredMode.REQUIRED)
  private Map<Language, String> message;
}