package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

  public static Specification<UserModel> hasCategoryId(Long categoryId) {
    return (root, query, builder) -> {
      if (categoryId == null) {
        return null;
      }
      Join<UserModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, CategoryModel> categoryJoin = articleJoin.join("categories",
          JoinType.INNER);
      return builder.and(builder.equal(categoryJoin.get("id"), categoryId),
          builder.equal(articleJoin.get("status"), Status.PUBLISHED));
    };
  }

  public static Specification<UserModel> hasRole(UserRole role) {
    return (root, query, builder) -> {
      if (role == null) {
        return builder.notEqual(root.get("role"), Role.ROLE_ADMIN);
      }
      return builder.equal(root.get("role"), role);
    };
  }

  public static Specification<UserModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      Join<UserModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, TagModel> tagJoin = articleJoin.join("tags", JoinType.INNER);
      return builder.equal(builder.lower(tagJoin.get("name")), tagName.toLowerCase());
    };
  }

  public static Specification<UserModel> hasInfoMatch(List<Long> usersIds) {
    return (root, query, builder) -> usersIds != null ? root.get("id").in(usersIds) : null;
  }

  public static Specification<UserModel> hasSubscriptions(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel == null || userModel.getSubscriptions().isEmpty()) {
        return builder.disjunction();
      }
      List<Long> subscriptionsIds = userModel.getSubscriptions().stream()
          .map(sub -> sub.getUser().getId()).toList();
      return root.get("id").in(subscriptionsIds);
    };
  }

  public static Specification<UserModel> hasSubscribers(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel == null || userModel.getSubscribers().isEmpty()) {
        return builder.disjunction();
      }
      List<Long> subscribersIds = userModel.getSubscribers().stream()
          .map(sub -> sub.getSubscriber().getId()).toList();
      return root.get("id").in(subscribersIds);
    };
  }

  public static Specification<UserModel> buildSpecification(Long categoryId, UserRole role,
      String tagName, List<Long> usersIds, Boolean isSubscriptions, Boolean isSubscribers,
      UserModel userModel) {
    Specification<UserModel> spec = Specification.where(hasCategoryId(categoryId))
        .and(hasRole(role)).and(hasTagName(tagName)).and(hasInfoMatch(usersIds));
    if (Boolean.TRUE.equals(isSubscriptions)) {
      spec = spec.and(hasSubscriptions(userModel));
    } else if (Boolean.TRUE.equals(isSubscribers)) {
      spec = spec.and(hasSubscribers(userModel));
    }
    return spec;
  }
}