package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateTag;
import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@Tag(name = "Tags", description = "API for blog tags")
public class TagController {

  private final TagService tagService;

  @GetMapping
  @Operation(summary = "Get tag by name")
  public ResponseEntity<List<GetTag>> getTagsByNameContaining(@RequestParam String name) {
    List<GetTag> tags = tagService.getTagsByNameContaining(name);
    return ResponseEntity.ok(tags);
  }

  @PostMapping
  @Operation(summary = "Create a new tag")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetTag> createTag(@Valid @RequestBody CreateTag tag) {
    GetTag createdTag = tagService.createTag(tag);
    return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
  }
}
