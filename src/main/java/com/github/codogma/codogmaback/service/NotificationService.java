package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateNotification;
import com.github.codogma.codogmaback.dto.GetNotification;
import com.github.codogma.codogmaback.dto.GetSystemNotification;
import com.github.codogma.codogmaback.dto.UpdateNotification;
import com.github.codogma.codogmaback.event.NotificationEvent;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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

  private final ApplicationEventPublisher eventPublisher;
  private final NotificationRepository notificationRepository;
  private final LocalizationUtil localizationUtil;

  @Transactional
  @Cacheable(value = "notifications", key = "{#order, #sort, #page, #size, #isRead, #userModel?.id}", unless = "#result == null || #result.isEmpty()")
  public Page<GetNotification> getNotifications(String order, String sort, int page, int size,
      Boolean isRead, UserModel userModel) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    Specification<NotificationModel> spec = NotificationSpecifications.buildSpecification(userModel,
        isRead);
    return notificationRepository.findAll(spec, pageable).map(this::convertNotificationModelToDTO);
  }

  @Transactional
  public GetSystemNotification getSystemNotificationById(Long notificationId, UserModel userModel) {
    String username = userModel != null ? userModel.getUsername() : null;
    NotificationModel notification = notificationRepository.findByIdAndRecipientOrTypeAndId(
            notificationId, username, NotificationType.SYSTEM, notificationId)
        .orElseThrow(() -> new RuntimeException("Notification not found"));
    return GetSystemNotification.builder().title(notification.getTitle())
        .message(notification.getMessage()).build();
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void createNotification(CreateNotification createNotification) {
    NotificationModel notification = NotificationModel.builder()
        .title(createNotification.getTitle()).message(createNotification.getMessage())
        .type(NotificationType.SYSTEM).isRead(false).build();
    saveAndSendToPublic(notification);
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void updateNotification(Long notificationId, UpdateNotification updateNotification) {
    NotificationModel notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new RuntimeException("Notification not found"));
    Optional.ofNullable(updateNotification.getTitle()).ifPresent(notification::setTitle);
    Optional.ofNullable(updateNotification.getMessage()).ifPresent(notification::setMessage);
    notification.setType(NotificationType.SYSTEM);
    notification.setRead(false);
    saveAndSendToPublic(notification);
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public GetNotification markAsRead(Long notificationId, UserModel userModel) {
    NotificationModel notification = notificationRepository.findByIdAndRecipient(notificationId,
        userModel.getUsername()).orElseThrow(() -> new RuntimeException("Notification not found"));
    notification.setRead(true);
    notification = notificationRepository.save(notification);
    return convertNotificationModelToDTO(notification);
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void markAllAsRead(UserModel userModel) {
    List<NotificationModel> notifications = notificationRepository.findByRecipientAndIsReadIsFalse(
        userModel.getUsername());
    notifications.forEach(notification -> notification.setRead(true));
    notificationRepository.saveAll(notifications);
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void deleteReadNotifications(UserModel userModel) {
    notificationRepository.deleteByRecipientAndIsReadIsTrue(userModel.getUsername());
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void deleteSystemNotification(Long id) {
    notificationRepository.deleteByTypeAndId(NotificationType.SYSTEM, id);
    GetNotification payload = GetNotification.builder().id(id).title("delete")
        .message("The system notification deleted").build();
    eventPublisher.publishEvent(new NotificationEvent(null, payload, false));
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void deleteNotification(Long id, UserModel userModel) {
    notificationRepository.deleteByRecipientAndTypeNotAndId(userModel.getUsername(),
        NotificationType.SYSTEM, id);
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void deleteAllSystemNotifications() {
    notificationRepository.deleteByType(NotificationType.SYSTEM);
    GetNotification payload = GetNotification.builder().title("delete")
        .message("All system notifications deleted").build();
    eventPublisher.publishEvent(new NotificationEvent(null, payload, false));
  }

  @Transactional
  @CacheEvict(value = "notifications", allEntries = true)
  public void saveAndSendToPrivate(String username, NotificationModel notification) {
    NotificationModel saved = notificationRepository.save(notification);
    GetNotification payload = convertNotificationModelToDTO(saved);
    eventPublisher.publishEvent(new NotificationEvent(username, payload, true));
  }

  @Transactional
  public void saveAndSendToPublic(NotificationModel notification) {
    NotificationModel saved = notificationRepository.save(notification);
    GetNotification payload = convertNotificationModelToDTO(saved);
    eventPublisher.publishEvent(new NotificationEvent(null, payload, false));
  }

  public GetNotification convertNotificationModelToDTO(NotificationModel notification) {
    String localizedNotificationTitle = localizationUtil.getLocalizedValue(notification.getTitle());
    String localizedNotificationMessage = localizationUtil.getLocalizedValue(
        notification.getMessage());
    return GetNotification.builder().id(notification.getId()).articleId(notification.getArticleId())
        .commentId(notification.getCommentId()).title(localizedNotificationTitle)
        .message(localizedNotificationMessage).type(notification.getType())
        .isRead(notification.isRead()).build();
  }
}
