package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {

  private static Specification<CategoryModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      Join<CategoryModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, TagModel> tagJoin = articleJoin.join("tags", JoinType.INNER);
      return builder.equal(builder.lower(tagJoin.get("name")), tagName.toLowerCase());
    };
  }

  private static Specification<CategoryModel> hasInfoMatch(List<Long> categoryIds) {
    return (root, query, builder) -> categoryIds != null ? root.get("id").in(categoryIds) : null;
  }

  private static Specification<CategoryModel> hasFavorites(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel == null || userModel.getFavorites().isEmpty()) {
        return builder.disjunction();
      }
      List<Long> favoriteCategoryIds = userModel.getFavorites().stream()
          .map(favorite -> favorite.getCategory().getId()).toList();
      return root.get("id").in(favoriteCategoryIds);
    };
  }

  public static Specification<CategoryModel> buildSpecification(String tagName,
      List<Long> categoryIds, Boolean isFavorite, UserModel userModel) {
    Specification<CategoryModel> spec = Specification.where(hasTagName(tagName))
        .and(hasInfoMatch(categoryIds));
    if (Boolean.TRUE.equals(isFavorite)) {
      spec = spec.and(hasFavorites(userModel));
    }
    return spec;
  }
}