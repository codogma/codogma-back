package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.codogma.codogmaback.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Comment")
@JsonInclude(Include.NON_NULL)
public class GetComment {

  @Schema(description = "ID of the comment", example = "1")
  private Long id;
  @Schema(description = "Status of the comment")
  private Status status;
  @Schema(description = "Content of the comment", example = "This is the content of the comment")
  private String content;
  @Schema(description = "User associated with the comment")
  private GetUser user;
  @Schema(description = "Creation date of the comment", example = "2022-01-01T00:00:00.000Z")
  private Instant createdAt;
  @Schema(description = "Replies to the comment")
  private List<GetComment> replies;
  @Schema(description = "Article associated with the comment")
  private GetArticle article;
}