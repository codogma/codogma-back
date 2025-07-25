package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateArticleImage;
import com.github.codogma.codogmaback.dto.PaletteDTO;
import com.github.codogma.codogmaback.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
@Tag(name = "Upload images", description = "API for uploading article images")
public class ImageUploadController {

  private final ImageUploadService imageUploadService;

  @PostMapping(value = "/upload/{articleId:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload image")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> upload(@PathVariable Long articleId,
      @RequestPart("image") MultipartFile image,
      @RequestPart(value = "palette", required = false) PaletteDTO palette,
      @RequestParam("isPreview") boolean isPreview) {
    CreateArticleImage createArticleImage = CreateArticleImage.builder().image(image)
        .isPreview(isPreview).palette(palette).build();
    String imageUrl = imageUploadService.uploadArticleImage(articleId, createArticleImage);
    return ResponseEntity.ok(imageUrl);
  }
}
