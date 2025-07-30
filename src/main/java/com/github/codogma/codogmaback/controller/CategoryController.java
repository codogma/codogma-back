package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateCategory;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetCategoryToUpdate;
import com.github.codogma.codogmaback.dto.UpdateCategory;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CategoryService;
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
@RequestMapping("/categories")
@Tag(name = "Categories", description = "API for categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(summary = "Get filtered categories")
  @Parameters({@Parameter(name = "tag", description = "Tag to filter categories"),
      @Parameter(name = "info", description = "Information to filter categories"),
      @Parameter(name = "isFavorite", description = "Get user's favorite categories"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of categories per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetCategory>> getCategories(@RequestParam(required = false) String tag,
      @RequestParam(required = false) String info,
      @RequestParam(required = false) Boolean isFavorite,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      //TODO исправить сортировку по названию
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetCategory> categories = categoryService.getCategories(order, sort, page, size, tag, info,
        isFavorite, userModel);
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/list-by-name")
  @Operation(summary = "Get categories by name")
  public ResponseEntity<List<GetCategory>> getCategoriesByName(@RequestParam String name) {
    List<GetCategory> categories = categoryService.getCategoriesByNameContaining(name);
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id:\\d+}")
  @Operation(summary = "Get the category by id")
  public ResponseEntity<GetCategory> getCategoryById(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetCategory categoryById = categoryService.getCategoryById(id, userModel);
    return ResponseEntity.ok(categoryById);
  }

  @GetMapping("/{id:\\d+}/to-update")
  @Operation(summary = "Get the category by id to update")
  public ResponseEntity<GetCategoryToUpdate> getCategoryByIdToUpdate(@PathVariable Long id) {
    return categoryService.getCategoryByIdToUpdate(id)
        .map(category -> new ResponseEntity<>(category, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new category")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<GetCategory> createCategory(
      @Valid @ModelAttribute CreateCategory createCategory,
      @AuthenticationPrincipal UserModel userModel) {
    GetCategory createdCategory = categoryService.createCategory(createCategory, userModel);
    return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update the category")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<GetCategory> updateCategory(@PathVariable Long id,
      @Valid @ModelAttribute UpdateCategory updateCategory,
      @AuthenticationPrincipal UserModel userModel) {
    GetCategory updatedCategory = categoryService.updateCategory(id, updateCategory, userModel);
    return ResponseEntity.ok(updatedCategory);
  }

  @DeleteMapping("/{id:\\d+}")
  @Operation(summary = "Delete the category")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return new ResponseEntity<>("Category deleted successfully", HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{id:\\d+}/add-to-favorites")
  @Operation(summary = "Add the category to favorites")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCategory> addToFavorites(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetCategory category = categoryService.addToFavorite(id, userModel);
    return ResponseEntity.ok(category);
  }

  @DeleteMapping("/{id:\\d+}/unfavorite")
  @Operation(summary = "Unfavorite the category")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCategory> unfavorite(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetCategory category = categoryService.unfavorite(id, userModel);
    return ResponseEntity.ok(category);
  }
}
