package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateUser;
import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "API for users")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Get filtered users")
  @Parameters({@Parameter(name = "categoryId", description = "Category id to filter authors"),
      @Parameter(name = "targetUsername", description = "Target username to get subscribers"),
      @Parameter(name = "role", description = "Role to filter users", schema = @Schema(implementation = UserRole.class)),
      @Parameter(name = "tag", description = "Tag to filter users"),
      @Parameter(name = "info", description = "Information to filter users"),
      @Parameter(name = "isSubscriptions", description = "Get user's subscriptions"),
      @Parameter(name = "isSubscribers", description = "Get user's subscribers"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of users per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetUser>> getUsers(@RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String targetUsername,
      @RequestParam(required = false) UserRole role, @RequestParam(required = false) String tag,
      @RequestParam(required = false) String info,
      @RequestParam(required = false) Boolean isSubscriptions,
      @RequestParam(required = false) Boolean isSubscribers,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "username") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetUser> users = userService.getUsers(categoryId, targetUsername, role, tag, info, page,
        size, sort, order, isSubscriptions, isSubscribers, userModel);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{username}")
  @Operation(summary = "Get the user by username")
  public ResponseEntity<GetUser> getUserByUsername(@PathVariable String username,
      @AuthenticationPrincipal UserModel userModel) {
    GetUser userByUsername = userService.getUserByUsername(username, userModel);
    return ResponseEntity.ok(userByUsername);
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update the user by username")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Object> updateUser(@Valid @ModelAttribute UpdateUser updateUser,
      @RequestParam(value = "avatar", required = false) MultipartFile avatar,
      @AuthenticationPrincipal UserModel userModel, BindingResult bindingResult) {
    updateUser.setAvatar(avatar);
    GetUser updatedUser = userService.updateUser(updateUser, userModel, bindingResult);
    if (bindingResult.hasErrors()) {
      Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(
          Collectors.toMap(FieldError::getField,
              fieldError -> Optional.ofNullable(fieldError.getDefaultMessage())
                  .orElse("Unknown error")));
      return ResponseEntity.badRequest().body(errors);
    }
    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
  }

  @DeleteMapping("/{username}")
  @Operation(summary = "Delete the user by username")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    userService.deleteUser(username);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{username}/subscribe")
  @Operation(summary = "Subscribe to the user")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetUser> subscribe(@PathVariable String username,
      @AuthenticationPrincipal UserModel userModel) {
    GetUser user = userService.subscribe(username, userModel);
    return ResponseEntity.ok(user);
  }

  @DeleteMapping("/{username}/unsubscribe")
  @Operation(summary = "Unsubscribe from the user")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetUser> unsubscribe(@PathVariable String username,
      @AuthenticationPrincipal UserModel userModel) {
    GetUser user = userService.unsubscribe(username, userModel);
    return ResponseEntity.ok(user);
  }
}
