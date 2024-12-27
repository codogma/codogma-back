package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecifications {

  public static Specification<ArticleModel> hasAccess(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        Role role = userModel.getRole();
        if (role.equals(Role.ROLE_ADMIN)) {
          return builder.conjunction();
        } else if (role.equals(Role.ROLE_AUTHOR)) {
          return builder.and(builder.or(builder.equal(root.get("status"), Status.PUBLISHED),
                  builder.equal(root.get("user").get("username"), userModel.getUsername())),
              builder.equal(root.get("user").get("role"), Role.ROLE_AUTHOR));
        }
      }
      return builder.and(builder.equal(root.get("status"), Status.PUBLISHED),
          builder.equal(root.get("user").get("role"), Role.ROLE_AUTHOR));
    };
  }

  public static Specification<ArticleModel> hasFeed(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel == null) {
        return builder.disjunction();
      }
      List<Long> favoriteCategoriesIds = userModel.getFavorites().stream()
          .map(favorite -> favorite.getCategory().getId()).toList();
      List<Long> subscribedUserIds = userModel.getSubscriptions().stream()
          .map(sub -> sub.getUser().getId()).toList();
      Predicate favoritePredicate = favoriteCategoriesIds.isEmpty() ? builder.disjunction()
          : root.join("categories").get("id").in(favoriteCategoriesIds);
      Predicate subscriptionPredicate = subscribedUserIds.isEmpty() ? builder.disjunction()
          : root.join("user").get("id").in(subscribedUserIds);
      return builder.or(favoritePredicate, subscriptionPredicate);
    };
  }

  public static Specification<ArticleModel> hasCategoryId(Long categoryId) {
    return (root, query, builder) -> categoryId != null ? builder.equal(
        root.join("categories").get("id"), categoryId) : null;
  }

  public static Specification<ArticleModel> hasCompilationId(Long compilationId) {
    return (root, query, builder) -> compilationId != null ? builder.equal(
        root.join("compilations").get("id"), compilationId) : null;
  }

  public static Specification<ArticleModel> hasContentMatch(List<Long> articleIds) {
    return (root, query, builder) -> articleIds != null ? root.get("id").in(articleIds) : null;
  }

  public static Specification<ArticleModel> hasSupportedLanguage(
      List<Language> supportedLanguages) {
    return (root, query, builder) -> root.get("language").in(supportedLanguages);
  }

  public static Specification<ArticleModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      return builder.equal(builder.lower(root.join("tags").get("name")), tagName.toLowerCase());
    };
  }

  public static Specification<ArticleModel> hasUsername(String username) {
    return (root, query, builder) -> username != null ? builder.equal(
        root.get("user").get("username"), username) : null;
  }

  public static Specification<ArticleModel> buildSpecification(Long categoryId, Long compilationId,
      String tagName, String username, List<Language> supportedLanguages, Boolean isFeed,
      UserModel userModel, List<Long> articleIds) {
    Specification<ArticleModel> spec = Specification.where(hasCategoryId(categoryId))
        .and(hasCompilationId(compilationId)).and(hasTagName(tagName)).and(hasUsername(username))
        .and(hasSupportedLanguage(supportedLanguages)).and(hasAccess(userModel))
        .and(hasContentMatch(articleIds));
    if (Boolean.TRUE.equals(isFeed)) {
      spec = spec.and(hasFeed(userModel));
    }
    return spec;
  }
}
