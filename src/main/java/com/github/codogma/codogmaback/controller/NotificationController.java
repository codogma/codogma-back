package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateNotificationDTO;
import com.github.codogma.codogmaback.dto.GetNotificationDTO;
import com.github.codogma.codogmaback.dto.GetSystemNotificationDTO;
import com.github.codogma.codogmaback.dto.UpdateNotificationDTO;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "API for notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(summary = "Get filtered notifications")
  @Parameters({@Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of notifications per page"),
      @Parameter(name = "isRead", description = "Filter by read status")})
  public ResponseEntity<Page<GetNotificationDTO>> getNotifications(
      @RequestParam(defaultValue = "desc") String order,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) Boolean isRead,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetNotificationDTO> notifications = notificationService.getNotifications(order, sort, page,
        size, isRead, userModel);
    return ResponseEntity.ok(notifications);
  }

  @GetMapping("/{id:\\d+}")
  @Operation(summary = "Get the system notification by id")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<GetSystemNotificationDTO> getSystemNotificationById(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetSystemNotificationDTO article = notificationService.getSystemNotificationById(id, userModel);
    return ResponseEntity.ok(article);
  }

  @PostMapping
  @Operation(summary = "Create the system notification")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> createSystemNotification(
      @Valid @RequestBody CreateNotificationDTO createNotification) {
    notificationService.createNotification(createNotification);
    return ResponseEntity.noContent().build();
  }

  @PutMapping(value = "/{id:\\d+}")
  @Operation(summary = "Update system notification by id")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> updateSystemNotification(@PathVariable Long id,
      @Valid @RequestBody UpdateNotificationDTO updateNotification) {
    notificationService.updateNotification(id, updateNotification);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id:\\d+}/read")
  @Operation(summary = "Mark the notification as read by id")
  public ResponseEntity<GetNotificationDTO> markAsRead(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetNotificationDTO readNotification = notificationService.markAsRead(id, userModel);
    return ResponseEntity.ok(readNotification);
  }

  @PatchMapping("/read-all")
  @Operation(summary = "Mark all notifications as read")
  public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserModel userModel) {
    notificationService.markAllAsRead(userModel);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/delete-read")
  @Operation(summary = "Delete all read notifications")
  public ResponseEntity<Void> deleteReadNotifications(
      @AuthenticationPrincipal UserModel userModel) {
    notificationService.deleteReadNotifications(userModel);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id:\\d+}/delete-system")
  @Operation(summary = "Delete the system notification by id")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteSystemNotification(@PathVariable Long id) {
    notificationService.deleteSystemNotification(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id:\\d+}/delete")
  @Operation(summary = "Delete the notification by id")
  public ResponseEntity<Void> deleteNotification(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    notificationService.deleteNotification(id, userModel);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/delete-all-system")
  @Operation(summary = "Delete all system notifications")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteSystemNotifications() {
    notificationService.deleteAllSystemNotifications();
    return ResponseEntity.noContent().build();
  }
}