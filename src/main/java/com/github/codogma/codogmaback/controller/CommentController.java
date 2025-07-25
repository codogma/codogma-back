package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateComment;
import com.github.codogma.codogmaback.dto.GetComment;
import com.github.codogma.codogmaback.dto.UpdateComment;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Tag(name = "Comments", description = "API for comments management")
public class CommentController {

  private final CommentService commentService;

  @GetMapping
  @Operation(summary = "Get filtered comments")
  @Parameters({@Parameter(name = "articleId", description = "Article id to filter comments"),
      @Parameter(name = "username", description = "Username to filter comments"),
      @Parameter(name = "content", description = "Content to filter comments"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of comments per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public Page<GetComment> getComments(@RequestParam(required = false) Long articleId,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String content, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    return commentService.getComments(articleId, username, content, page, size, sort, order,
        userModel);
  }

  @PostMapping
  @Operation(summary = "Add a new comment")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<GetComment> createComment(@Valid @RequestBody CreateComment createComment,
      @AuthenticationPrincipal UserModel userModel) {
    GetComment comment = commentService.createComment(createComment, userModel);
    return new ResponseEntity<>(comment, HttpStatus.CREATED);
  }

  @PatchMapping("/{commentId}/publish")
  @Operation(summary = "Publish the comment")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public GetComment publishComment(@PathVariable Long commentId) {
    return commentService.publishComment(commentId);
  }

  @PatchMapping("/{commentId}/block")
  @Operation(summary = "Block the comment")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public GetComment blockComment(@PathVariable Long commentId) {
    return commentService.blockComment(commentId);
  }

  @PutMapping("/{commentId}")
  @Operation(summary = "Update the comment")
  @PreAuthorize("isAuthenticated()")
  public GetComment updateComment(@PathVariable Long commentId,
      @Valid @RequestBody UpdateComment updateComment,
      @AuthenticationPrincipal UserModel userModel) {
    return commentService.updateComment(commentId, updateComment, userModel);
  }

  @DeleteMapping("/{commentId}")
  @Operation(summary = "Delete the comment")
  @PreAuthorize("isAuthenticated()")
  public void deleteComment(@PathVariable Long commentId,
      @AuthenticationPrincipal UserDetails userDetails) {
    commentService.deleteComment(commentId, userDetails);
  }
}
