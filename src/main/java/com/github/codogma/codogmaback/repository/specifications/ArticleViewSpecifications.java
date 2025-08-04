package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleView;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ArticleViewSpecifications {

  public static Specification<ArticleView> hasAccess(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        Role role = userModel.getRole();
        if (role.equals(Role.ROLE_ADMIN)) {
          return builder.conjunction();
        } else if (role.equals(Role.ROLE_AUTHOR)) {
          return builder.and(
              builder.or(builder.equal(root.get("article").get("status"), Status.PUBLISHED),
                  builder.equal(root.get("article").get("user").get("username"),
                      userModel.getUsername())),
              builder.equal(root.get("article").get("user").get("role"), Role.ROLE_AUTHOR));
        }
      }
      return builder.and(builder.equal(root.get("article").get("status"), Status.PUBLISHED),
          builder.equal(root.get("article").get("user").get("role"), Role.ROLE_AUTHOR));
    };
  }

  public static Specification<ArticleView> belongsToUser(UserModel userModel) {
    return (root, query, builder) -> builder.equal(root.get("user"), userModel);
  }

  public static Specification<ArticleView> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      return builder.equal(builder.lower(root.get("article").get("tags").get("name")),
          tagName.toLowerCase());
    };
  }

  public static Specification<ArticleView> hasContentMatch(List<Long> articleIds) {
    return (root, query, builder) -> articleIds != null ? root.get("article").get("id")
        .in(articleIds) : null;
  }

  public static Specification<ArticleView> buildSpecification(String tagName, List<Long> articleIds,
      UserModel userModel) {
    return Specification.where(belongsToUser(userModel)).and(hasTagName(tagName))
        .and(hasAccess(userModel)).and(hasContentMatch(articleIds));
  }
}