package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateNotification;
import com.github.codogma.codogmaback.dto.GetNotification;
import com.github.codogma.codogmaback.dto.UpdateNotification;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.NotificationRepository;
import com.github.codogma.codogmaback.repository.specifications.NotificationSpecifications;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
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
  private final MessageSource messageSource;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;

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
  public void updateNotification(Long notificationId, UpdateNotification updateNotification) {
    NotificationModel notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new RuntimeException("Notification not found"));
    Optional.ofNullable(updateNotification.getTitle()).ifPresent(notification::setTitle);
    Optional.ofNullable(updateNotification.getMessage()).ifPresent(notification::setMessage);
    notification.setType(updateNotification.getType());
    notification.setRead(false);
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
  public void deleteNotification(Long id, UserModel userModel) {
    notificationRepository.deleteByRecipientAndTypeNotAndId(userModel.getUsername(),
        NotificationType.SYSTEM, id);
  }

  @Transactional
  public void deleteAllSystemNotifications() {
    notificationRepository.deleteByType(NotificationType.SYSTEM);
  }

  private GetNotification convertNotificationModelToDTO(NotificationModel notification) {
    String localizedNotificationTitle = localizationUtil.getLocalizedValue(notification.getTitle());
    String localizedNotificationMessage = localizationUtil.getLocalizedValue(
        notification.getMessage());
    return GetNotification.builder().id(notification.getId()).articleId(notification.getArticleId())
        .commentId(notification.getCommentId()).title(localizedNotificationTitle)
        .message(localizedNotificationMessage).type(notification.getType())
        .isRead(notification.isRead()).build();
  }
}
