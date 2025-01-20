package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateNotification;
import com.github.codogma.codogmaback.dto.GetNotification;
import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.NotificationRepository;
import com.github.codogma.codogmaback.repository.specifications.NotificationSpecifications;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public Page<GetNotification> getNotifications(String order, String sort, int page, int size,
      Boolean isRead, UserModel userModel) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    Specification<NotificationModel> spec = NotificationSpecifications.buildSpecification(userModel,
        isRead);
    return notificationRepository.findAll(spec, pageable).map(this::convertNotificationModelToDTO);
  }

  @Transactional
  public GetNotification getNotificationById(Long notificationId, UserModel userModel) {
    String username = userModel != null ? userModel.getUsername() : null;
    NotificationModel notification = notificationRepository.findByIdAndRecipientOrTypeAndId(
            notificationId, username, NotificationType.SYSTEM, notificationId)
        .orElseThrow(() -> new RuntimeException("Notification not found"));
    return convertNotificationModelToDTO(notification);
  }

  @Transactional
  public void createNotification(CreateNotification createNotification) {
    NotificationModel notification = NotificationModel.builder()
        .title(createNotification.getTitle()).message(createNotification.getMessage())
        .type(createNotification.getType()).isRead(false).build();
    notificationRepository.save(notification);
  }

  @Transactional
  public GetNotification markAsRead(Long notificationId, UserModel userModel) {
    NotificationModel notification = notificationRepository.findByIdAndRecipient(notificationId,
        userModel.getUsername()).orElseThrow(() -> new RuntimeException("Notification not found"));
    notification.setRead(true);
    notification = notificationRepository.save(notification);
    return convertNotificationModelToDTO(notification);
  }

  @Transactional
  public void markAllAsRead(UserModel userModel) {
    List<NotificationModel> notifications = notificationRepository.findByRecipientAndIsReadIsFalse(
        userModel.getUsername());
    notifications.forEach(notification -> notification.setRead(true));
    notificationRepository.saveAll(notifications);
  }

  @Transactional
  public void deleteReadNotifications(UserModel userModel) {
    notificationRepository.deleteByRecipientAndIsReadIsTrue(userModel.getUsername());
  }

  @Transactional
  public void deleteSystemNotification(Long id) {
    notificationRepository.deleteByTypeAndId(NotificationType.SYSTEM, id);
  }

  @Transactional
  public void deleteAllSystemNotifications() {
    notificationRepository.deleteByType(NotificationType.SYSTEM);
  }

  private GetNotification convertNotificationModelToDTO(NotificationModel notificationModel) {
    return GetNotification.builder().id(notificationModel.getId())
        .articleId(notificationModel.getArticleId()).commentId(notificationModel.getCommentId())
        .title(notificationModel.getTitle()).message(notificationModel.getMessage())
        .type(notificationModel.getType()).isRead(notificationModel.isRead()).build();
  }
}
