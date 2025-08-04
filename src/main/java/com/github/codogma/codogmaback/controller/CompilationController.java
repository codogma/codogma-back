package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateCompilation;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.UpdateCompilation;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CompilationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
@Tag(name = "Compilations", description = "API for compilations")
public class CompilationController {

  private final CompilationService compilationService;

  @GetMapping
  @Operation(summary = "Get filtered compilations")
  @Parameters({@Parameter(name = "tag", description = "Tag to filter compilations"),
      @Parameter(name = "content", description = "Information to filter compilations"),
      @Parameter(name = "username", description = "Owner's username to filter articles"),
      @Parameter(name = "isBookmarked", description = "Get bookmarked compilations"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of compilations per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetCompilation>> getCompilations(
      @RequestParam(required = false) String tag, @RequestParam(required = false) String content,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Boolean isBookmarked,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "updatedAt") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetCompilation> compilations = compilationService.getCompilations(tag, content, username,
        isBookmarked, page, size, sort, order, userModel);
    return ResponseEntity.ok(compilations);
  }

  @GetMapping("/list-by-title")
  @Operation(summary = "Get compilations by title")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<List<GetCompilation>> getCompilationsByTitle(@RequestParam String title,
      @AuthenticationPrincipal UserModel userModel) {
    List<GetCompilation> compilations = compilationService.getCompilationsByTitle(title, userModel);
    return ResponseEntity.ok(compilations);
  }

  @GetMapping("/{id:\\d+}")
  @Operation(summary = "Get the compilation by id")
  public ResponseEntity<GetCompilation> getCompilationById(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    return compilationService.getCompilationById(id, userModel)
        .map(compilation -> new ResponseEntity<>(compilation, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new compilation")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCompilation> createCompilation(
      @Valid @ModelAttribute CreateCompilation createCompilation,
      @AuthenticationPrincipal UserModel userModel) {
    GetCompilation createdCompilation = compilationService.createCompilation(createCompilation,
        userModel);
    return new ResponseEntity<>(createdCompilation, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update the compilation")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCompilation> updateCompilation(@PathVariable Long id,
      @Valid @ModelAttribute UpdateCompilation updateCompilation,
      @AuthenticationPrincipal UserModel userModel) {
    GetCompilation updatedCompilation = compilationService.updateCompilation(id, updateCompilation,
        userModel);
    return ResponseEntity.ok(updatedCompilation);
  }

  @DeleteMapping("/{id:\\d+}")
  @Operation(summary = "Delete the compilation")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<String> deleteCompilation(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    compilationService.deleteCompilation(id, userModel);
    return new ResponseEntity<>("Compilation deleted successfully", HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{id:\\d+}/bookmark")
  @Operation(summary = "Add the compilation to bookmarks")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCompilation> bookmark(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetCompilation bookmarked = compilationService.bookmark(id, userModel);
    return ResponseEntity.ok(bookmarked);
  }

  @DeleteMapping("/{id:\\d+}/unbookmark")
  @Operation(summary = "Delete the compilation from bookmarks")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCompilation> unbookmark(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetCompilation unbookmarked = compilationService.unbookmark(id, userModel);
    return ResponseEntity.ok(unbookmarked);
  }
}