package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.exception.UserIdNotFoundException;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.ArticleViewModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.ArticleViewRepository;
import com.github.codogma.codogmaback.repository.FavoriteRepository;
import com.github.codogma.codogmaback.repository.SubscriptionRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentBasedRecommender {

  private final ArticleViewRepository articleViewRepository;
  private final EntityManager entityManager;
  private final FavoriteRepository favoriteRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;

  @Value("${recommendation.limit:5}")
  private int recommendationLimit;

  public List<ArticleModel> getRecommendations(Long userId) {
    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new UserIdNotFoundException("User not found with id: " + userId));

    // 1. Собираем данные о предпочтениях пользователя
    UserProfileVector profile = buildUserProfile(user);

    // 2. Поиск по индексу Lucene
    return searchSimilarArticles(profile);
  }

  private List<ArticleModel> searchSimilarArticles(UserProfileVector profile) {
    SearchSession searchSession = Search.session(entityManager);
    List<Long> categoryIds = profile.getCategoryIds();
    List<Long> authorIds = profile.getAuthorIds();
    List<Long> tagIds = profile.getTagIds();
    List<Long> viewedIds = profile.getViewedArticleIds();

    return searchSession.search(ArticleModel.class).where(f -> {
      BooleanPredicateClausesStep<?> bool = f.bool();
      // Добавляем условия только если есть данные
      if (!tagIds.isEmpty()) {
        bool.should(f.terms().fields("tags.id").matchingAny(tagIds));
      }
      if (!categoryIds.isEmpty()) {
        bool.should(f.terms().field("categories.id").matchingAny(categoryIds));
      }
      if (!authorIds.isEmpty()) {
        bool.should(f.terms().field("user.id").matchingAny(authorIds));
      }

      bool.must(f.match().field("status").matching(Status.PUBLISHED));

      if (!viewedIds.isEmpty()) {
        bool.mustNot(f.terms().field("id").matchingAny(viewedIds));
      }
      return bool;
    }).sort(f -> f.composite(b -> {
      b.add(f.score().desc());
      b.add(f.field("likesCount").desc());
    })).fetchHits(recommendationLimit);
  }

  private UserProfileVector buildUserProfile(UserModel user) {
    UserProfileVector vector = new UserProfileVector();

    // История просмотров
    List<ArticleViewModel> views = articleViewRepository.findTop20ByUserOrderByUpdatedAtDesc(user);
    vector.addAllTags(
        views.stream().flatMap(v -> v.getArticle().getTags().stream()).map(TagModel::getId)
            .collect(Collectors.toList()));

    vector.addAllCategories(views.stream().flatMap(v -> v.getArticle().getCategories().stream())
        .map(CategoryModel::getId).collect(Collectors.toList()));

    vector.addAllViewedArticleIds(
        views.stream().map(view -> view.getArticle().getId()).collect(Collectors.toList()));

    // Избранные категории
    Set<CategoryModel> favCategories = favoriteRepository.findByUser(user).stream()
        .map(FavoriteModel::getCategory).collect(Collectors.toSet());
    vector.addAllCategories(
        favCategories.stream().map(CategoryModel::getId).collect(Collectors.toSet()));

    // Подписки на авторов
    Set<UserModel> subscribedAuthors = subscriptionRepository.findBySubscriber(user).stream()
        .map(SubscriptionModel::getUser).collect(Collectors.toSet());
    vector.addAllAuthors(
        subscribedAuthors.stream().map(UserModel::getId).collect(Collectors.toSet()));

    return vector;
  }

  // Вспомогательный класс для профиля
  private static class UserProfileVector {

    private final Set<Long> tagIds = new HashSet<>();
    private final Set<Long> categoryIds = new HashSet<>();
    private final Set<Long> authorIds = new HashSet<>();
    private final Set<Long> viewedArticleIds = new HashSet<>();

    public void addAllTags(Collection<Long> tagIds) {
      this.tagIds.addAll(tagIds);
    }

    public void addAllCategories(Collection<Long> categoryIds) {
      this.categoryIds.addAll(categoryIds);
    }

    public void addAllAuthors(Collection<Long> authorIds) {
      this.authorIds.addAll(authorIds);
    }

    public void addAllViewedArticleIds(Collection<Long> viewedArticleIds) {
      this.viewedArticleIds.addAll(viewedArticleIds);
    }

    public List<Long> getTagIds() {
      return tagIds.isEmpty() ? Collections.emptyList() : new ArrayList<>(tagIds);
    }

    public List<Long> getCategoryIds() {
      return categoryIds.isEmpty() ? Collections.emptyList() : new ArrayList<>(categoryIds);
    }

    public List<Long> getAuthorIds() {
      return authorIds.isEmpty() ? Collections.emptyList() : new ArrayList<>(authorIds);
    }

    public List<Long> getViewedArticleIds() {
      return viewedArticleIds.isEmpty() ? Collections.emptyList()
          : new ArrayList<>(viewedArticleIds);
    }
  }
}