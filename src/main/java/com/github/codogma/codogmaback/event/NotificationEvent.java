package com.github.codogma.codogmaback.event;

import com.github.codogma.codogmaback.dto.GetNotificationDTO;

/**
 * @param username  для приватных уведомлений (если нужно)
 * @param isPrivate если false – публичное уведомление
 */
public record NotificationEvent(String username, GetNotificationDTO payload, boolean isPrivate) {

}