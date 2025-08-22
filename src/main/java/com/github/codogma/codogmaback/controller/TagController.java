package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@Tag(name = "Tags", description = "API for tags")
public class TagController {

  private final TagService tagService;

  @GetMapping
  @Operation(summary = "Get tags by name")
  public ResponseEntity<List<GetTag>> getTagsByNameContaining(@RequestParam String name) {
    List<GetTag> tags = tagService.getTagsByNameContaining(name);
    return ResponseEntity.ok(tags);
  }
}