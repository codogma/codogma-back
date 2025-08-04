package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CompilationSpecifications {

  private static Specification<CompilationModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      Join<CompilationModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, TagModel> tagJoin = articleJoin.join("tags", JoinType.INNER);
      return builder.equal(builder.lower(tagJoin.get("name")), tagName.toLowerCase());
    };
  }

  private static Specification<CompilationModel> hasInfoMatch(List<Long> compilationIds) {
    return (root, query, builder) -> compilationIds != null ? root.get("id").in(compilationIds)
        : null;
  }

  private static Specification<CompilationModel> hasBookmarks(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel == null || userModel.getBookmarks().isEmpty()) {
        return builder.disjunction();
      }
      List<Long> bookmarkCategoryIds = userModel.getBookmarks().stream()
          .map(bookmark -> bookmark.getCompilation().getId()).toList();
      return root.get("id").in(bookmarkCategoryIds);
    };
  }

  public static Specification<CompilationModel> hasUsername(String username) {
    return (root, query, builder) -> username != null ? builder.equal(
        root.get("user").get("username"), username) : null;
  }

  public static Specification<CompilationModel> buildSpecification(String tagName, String username,
      Boolean isBookmarked, UserModel userModel, List<Long> compilationIds) {
    Specification<CompilationModel> spec = Specification.where(hasTagName(tagName))
        .and(hasUsername(username)).and(hasInfoMatch(compilationIds));
    if (Boolean.TRUE.equals(isBookmarked)) {
      spec = spec.and(hasBookmarks(userModel));
    }
    return spec;
  }
}