package com.github.codogma.codogmaback.event;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final SimpMessagingTemplate messagingTemplate;
  public static final String PRIVATE_NOTIFICATIONS = "/queue/notifications";
  public static final String PUBLIC_NOTIFICATIONS = "/topic/public-notifications";

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationEvent(NotificationEvent event) {
    if (event.isPrivate()) {
      // Отправляем приватное уведомление конкретному пользователю
      messagingTemplate.convertAndSendToUser(event.username(), PRIVATE_NOTIFICATIONS,
          event.payload()
      );
    } else {
      // Отправляем публичное уведомление (broadcast)
      messagingTemplate.convertAndSend(PUBLIC_NOTIFICATIONS, event.payload());
    }
  }
}