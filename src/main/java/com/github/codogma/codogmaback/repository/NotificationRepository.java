package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long>,
    JpaSpecificationExecutor<NotificationModel> {

  Optional<NotificationModel> findByIdAndRecipient(Long id, String recipient);

  Optional<NotificationModel> findByIdAndRecipientOrTypeAndId(Long id1, String recipient,
      NotificationType type, Long id2);

  List<NotificationModel> findByRecipientAndIsReadIsFalse(String recipient);

  void deleteByRecipientAndIsReadIsTrue(String recipient);

  void deleteByRecipientAndTypeNotAndId(String recipient, NotificationType type, Long id);

  void deleteByTypeAndId(NotificationType type, Long id);

  void deleteByType(NotificationType type);
}
