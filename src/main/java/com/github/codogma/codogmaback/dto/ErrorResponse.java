package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Error Response")
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

  private String errorCode;
  private String message;
  private boolean reauthenticate;
  private Instant timestamp = Instant.now();
  private String path;
}
