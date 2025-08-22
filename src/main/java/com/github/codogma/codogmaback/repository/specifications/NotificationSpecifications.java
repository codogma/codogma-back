package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import com.github.codogma.codogmaback.model.UserModel;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecifications {

  public static Specification<NotificationModel> hasAccess(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        String username = userModel.getUsername();
        return builder.or(builder.equal(root.get("recipient"), username),
            builder.equal(root.get("type"), NotificationType.SYSTEM));
      }
      return builder.equal(root.get("type"), NotificationType.SYSTEM);
    };
  }

  public static Specification<NotificationModel> hasRead(Boolean isRead) {
    return (root, query, builder) -> isRead != null ? builder.equal(root.get("isRead"), isRead)
        : null;
  }

  public static Specification<NotificationModel> buildSpecification(UserModel userModel,
      Boolean isRead) {
    return Specification.where(hasAccess(userModel)).and(hasRead(isRead));
  }
}