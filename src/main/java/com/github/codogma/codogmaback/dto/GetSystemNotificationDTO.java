package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.codogma.codogmaback.model.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get System Notification")
@JsonInclude(Include.NON_NULL)
public class GetSystemNotificationDTO {

  @Schema(description = "Title of the notification", example = "{ \"en\": \"System Update\", \"ru\": \"Системное обновление\" }")
  private Map<Language, String> title;
  @Schema(description = "Message of the notification", example = "{ \"en\": \"The system will be updated at 2 AM.\", \"ru\": \"Система будет обновлена в 2 часа ночи.\" }")
  private Map<Language, String> message;
}