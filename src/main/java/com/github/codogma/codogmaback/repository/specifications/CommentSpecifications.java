package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.CommentModel;
import com.github.codogma.codogmaback.model.Status;
import org.springframework.data.jpa.domain.Specification;

public class CommentSpecifications {

  public static Specification<CommentModel> hasAccess(String username, boolean isAdmin,
      String articleAuthorUsername) {
    return (root, query, builder) -> {
      if (isAdmin || (username != null && username.equals(articleAuthorUsername))) {
        return builder.conjunction();
      } else {
        return builder.equal(root.get("status"), Status.PUBLISHED);
      }
    };
  }

  public static Specification<CommentModel> hasArticleId(Long articleId) {
    return (root, query, builder) -> builder.equal(root.get("article").get("id"), articleId);
  }

  public static Specification<CommentModel> isRootComment() {
    return (root, query, builder) -> builder.isNull(root.get("parentComment"));
  }

  public static Specification<CommentModel> belongsToUser(Long userId) {
    return (root, query, builder) -> builder.equal(root.get("user").get("id"), userId);
  }

  public static Specification<CommentModel> isAccessible(boolean canViewAllComments) {
    return (root, query, builder) -> {
      if (canViewAllComments) {
        return builder.conjunction();
      } else {
        return builder.equal(root.get("status"), Status.PUBLISHED);
      }
    };
  }
}