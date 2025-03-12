package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CompilationsDTO;
import com.github.codogma.codogmaback.dto.CreateDraftArticle;
import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.UpdateArticle;
import com.github.codogma.codogmaback.dto.UpdateDraftArticle;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.exception.LikeAlreadyExistsException;
import com.github.codogma.codogmaback.exception.LikeNotFoundException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.ArticleScoreProjection;
import com.github.codogma.codogmaback.model.ArticleView;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.LikeModel;
import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.ArticleRepository;
import com.github.codogma.codogmaback.repository.ArticleViewRepository;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.CompilationRepository;
import com.github.codogma.codogmaback.repository.LikeRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.ArticleSpecifications;
import com.github.codogma.codogmaback.repository.specifications.ArticleViewSpecifications;
import static com.github.codogma.codogmaback.util.ContentUtil.createHtmlPreview;
import com.github.codogma.codogmaback.util.KeywordExtractor;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.engine.search.common.BooleanOperator;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final CategoryRepository categoryRepository;
  private final CompilationRepository compilationRepository;
  private final KeywordExtractor keywordExtractor;
  private final LikeRepository likeRepository;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;
  private final NotificationService notificationService;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final ContentBasedRecommender contentBasedRecommender;
  private final JCacheCacheManager jCacheCacheManager;

  @Value("${search.results.limit}")
  private int searchResultsLimit;
  @Value("${recommendation.limit:5}")
  private int recommendationLimit;

  @Transactional
  @Cacheable(value = "articles", key = "T(java.util.Objects).hash(#order, #sort, #page, #size, #categoryId, #compilationId, #tag, #username, #isFeed, #content, #userModel?.id)", condition = "#content == null || #content.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetArticle> getArticles(String order, String sort, int page, int size,
      Long categoryId, Long compilationId, String tag, String username, Boolean isFeed,
      UserModel userModel, String content) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> exceptionFactory.userNotFound(userModel.getUsername())) : null;
    List<Language> supportedLanguages = localizationContext.getSupportedLanguages();
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> articleIds = getArticleIds(content);
    Specification<ArticleModel> spec = ArticleSpecifications.buildSpecification(categoryId,
        compilationId, tag, username, supportedLanguages, isFeed, foundUser, articleIds);
    return articleRepository.findAll(spec, pageable)
        .map(articleModel -> convertArticleModelToDTO(articleModel, userModel))
        .map(this::preparePreview);
  }

  @Transactional
  @Cacheable(value = "viewedArticles", key = "T(java.util.Objects).hash(#order, #sort, #page, #size, #tag, #content, #userModel?.id)", condition = "#content == null || #content.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetArticle> getViewedArticles(String order, String sort, int page, int size,
      String tag, String content, UserModel userModel) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> articleIds = getArticleIds(content);
    Specification<ArticleView> spec = ArticleViewSpecifications.buildSpecification(tag, articleIds,
        userModel);
    Page<ArticleView> views = articleViewRepository.findAll(spec, pageable);
    return views.map(view -> convertArticleModelToDTO(view.getArticle(), userModel))
        .map(this::preparePreview);
  }

  private List<Long> getArticleIds(String content) {
    List<Long> articleIds = null;
    if (content != null && !content.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      articleIds = searchSession.search(ArticleModel.class)
          .where(f -> f.match().fields("content", "title").matching(content).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(ArticleModel::getId).toList();
    }
    return articleIds;
  }

  private GetArticle preparePreview(GetArticle article) {
    if (article.getPreviewContent() != null && article.getPreviewContent().isEmpty()) {
      String previewContent = createHtmlPreview(article.getContent(), 1100);
      article.setPreviewContent(previewContent);
    } else {
      article.setPreviewContent(article.getPreviewContent());
    }
    article.setContent(null);
    return article;
  }

  @Transactional
  public List<GetArticle> getDraftArticles(UserModel userModel) {
    return articleRepository.findAllByUserAndStatus(userModel, Status.DRAFT).stream()
        .map(articleModel -> convertArticleModelToDTO(articleModel, userModel)).toList();
  }

  @Transactional
  @Cacheable(value = "articleById", key = "{#articleId, #userModel?.id}")
  public GetArticle getArticleById(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.PUBLISHED && userModel == null) {
      throw exceptionFactory.articleNotFound(articleId);
    }
    if (!articleStatus.equals(Status.PUBLISHED) && !articleModel.getUser().getUsername()
        .equals(userModel.getUsername()) && !userModel.getRole().equals(Role.ROLE_ADMIN)) {
      throw exceptionFactory.articleNotFound(articleId);
    }
    return convertArticleModelToDTO(articleModel, userModel);
  }

  @Transactional
  @CacheEvict(cacheNames = "articleById", allEntries = true)
  public void like(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Optional<LikeModel> existingLike = likeRepository.findByArticleAndUser(article, userModel);
    if (existingLike.isPresent()) {
      throw new LikeAlreadyExistsException("The article was already liked");
    }
    LikeModel like = LikeModel.builder().article(article).user(userModel).build();
    likeRepository.save(like);
    article.setLikeCount(article.getLikeCount() + 1);
    articleRepository.save(article);
  }

  @Transactional
  @CacheEvict(cacheNames = "articleById", allEntries = true)
  public void unlike(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    LikeModel existingLike = likeRepository.findByArticleAndUser(article, userModel)
        .orElseThrow(() -> new LikeNotFoundException("Like not found"));
    likeRepository.delete(existingLike);
    article.setLikeCount(article.getLikeCount() - 1);
    articleRepository.save(article);
  }

  @Transactional
  @CacheEvict(value = "viewedArticles", key = "#articleId")
  public GetArticle recordView(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (userModel != null) {
      ArticleView existingView = articleViewRepository.findByUserAndArticle(userModel, articleModel)
          .orElseGet(() -> ArticleView.builder().user(userModel).article(articleModel).build());
      existingView.setUpdatedAt(LocalDateTime.now());
      articleViewRepository.save(existingView);
    }
    return convertArticleModelToDTO(articleModel, userModel);
  }

  @Transactional
  @Cacheable(value = "recommendations", key = "#articleId")
  public List<GetArticle> getRecommendationsForArticle(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    List<Long> categoryNames = article.getCategories().stream().map(CategoryModel::getId).toList();
    Set<String> tagNames = article.getTags().stream().map(TagModel::getName)
        .collect(Collectors.toSet());
    Set<String> titleKeywords = new HashSet<>(keywordExtractor.extractKeywords(article.getTitle()));
    titleKeywords.addAll(tagNames);
    String combinedKeywords = String.join(" ", titleKeywords);
    SearchSession searchSession = Search.session(entityManager);
    SearchResult<ArticleScoreProjection> result = searchSession.search(ArticleModel.class)
        .select(f -> f.composite().from(f.score(), f.entity()).as(ArticleScoreProjection::new))
        .where(f -> {
          BooleanPredicateClausesStep<?> bool = f.bool()
              .must(f.match().field("status").matching(Status.PUBLISHED))
              .mustNot(f.match().field("id").matching(articleId))
              .must(f.range().field("createdAt").atLeast(LocalDateTime.now().minusMonths(6)))
              .should(f.simpleQueryString().fields("title", "tags.name").matching(combinedKeywords)
                  .defaultOperator(BooleanOperator.OR).boost(1.0f))
              .must(f.match().field("title").matching(article.getTitle()).fuzzy().boost(10.0f))
              .should(f.match().field("content").matching(article.getContent()).boost(0.00002f));
          if (article.getLikeCount() != null) {
            bool.should(
                f.range().field("likeCount").atLeast(Math.round(article.getLikeCount() * 0.8f))
                    .boost(1.2f));
          }
          if (!tagNames.isEmpty()) {
            bool.should(f.terms().fields("tags.name").matchingAny(tagNames)
                .boost(tagNames.size() > 3 ? 5.0f : 1.0f));
          }
          if (!categoryNames.isEmpty()) {
            bool.should(f.terms().fields("categories.id").matchingAny(categoryNames)
                .boost(categoryNames.size() > 1 ? 5.0f : 3.5f));
          }
          bool.minimumShouldMatchNumber(1);
          return bool;
        }).sort(f -> f.composite(b -> {
          b.add(f.score().desc());
          b.add(f.field("likeCount").desc());
          b.add(f.field("createdAt").desc());
        })).fetch(recommendationLimit);
    List<ArticleModel> recommendedArticles = result.hits().stream()
        .peek(hit -> log.info("Score: {} {}", hit.score(), hit.articleModel().getTitle()))
        .filter(hit -> hit.score() > 35.0f).map(ArticleScoreProjection::articleModel).toList();
    return recommendedArticles.stream()
        .map(articleModel -> convertArticleModelToDTO(articleModel, userModel))
        .map(this::preparePreview).toList();
  }

  // TODO: fix this method
  @Transactional
  public List<GetArticle> getRecommendations(UserModel user) {
    if (user == null) {
      return Collections.emptyList();
    }
    List<ArticleModel> recommendations = contentBasedRecommender.getRecommendations(user.getId());
    return recommendations.stream().map(article -> convertArticleModelToDTO(article, user))
        .map(this::preparePreview).collect(Collectors.toList());
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "{#articleId, #userModel.id}", condition = "#userModel != null")
  public GetArticle getDraftedArticleById(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(articleId);
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.BLOCKED) {
      throw exceptionFactory.editingNotAllowed(articleId);
    }
    articleModel.setStatus(Status.DRAFT);
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle, userModel);
  }

  @Transactional
  public GetArticle createDraftArticle(CreateDraftArticle draftArticle, UserModel userModel) {
    ArticleModel articleModel = ArticleModel.builder().user(userModel)
        .title(draftArticle.getTitle()).content(draftArticle.getContent()).likeCount(0).build();
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle, userModel);
  }

  @Transactional
  public void updateDraftArticle(Long articleId, UpdateDraftArticle draftArticle,
      UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(articleId);
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.DRAFT) {
      throw exceptionFactory.editingNotAllowed(articleId);
    }
    Language language = draftArticle.getLanguage();
    if (language != null) {
      articleModel.setLanguage(draftArticle.getLanguage());
    }
    Long originalArticleId = draftArticle.getOriginalArticleId();
    if (originalArticleId != null) {
      articleRepository.findById(originalArticleId)
          .orElseThrow(() -> exceptionFactory.originalArticleNotFound(originalArticleId));
      articleModel.setOriginalArticleId(originalArticleId);
    }
    if (draftArticle.getTitle() != null) {
      articleModel.setTitle(draftArticle.getTitle());
    }
    if (draftArticle.getPreviewContent() != null) {
      articleModel.setPreviewContent(draftArticle.getPreviewContent());
    }
    if (draftArticle.getContent() != null) {
      articleModel.setContent(draftArticle.getContent());
    }
    List<Long> categoryIds = draftArticle.getCategoryIds();
    if (categoryIds != null && !categoryIds.isEmpty()) {
      List<CategoryModel> categories = new ArrayList<>(categoryRepository.findAllById(categoryIds));
      articleModel.setCategories(categories);
    }
    List<Long> compilationIds = draftArticle.getCompilationIds();
    if (compilationIds != null && !compilationIds.isEmpty()) {
      List<CompilationModel> compilations = new ArrayList<>(
          compilationRepository.findAllByIdInAndUser(compilationIds, userModel));
      if (!compilations.isEmpty()) {
        articleModel.setCompilations(compilations);
      }
    }
    List<String> tags = draftArticle.getTags();
    if (tags != null && !tags.isEmpty()) {
      List<TagModel> tagModels = new ArrayList<>();
      List<TagModel> existingTags = tagRepository.findAllByNameIgnoreCaseIn(tags);
      Map<String, TagModel> existingTagMap = existingTags.stream()
          .collect(Collectors.toMap(tag -> tag.getName().toLowerCase().trim(), tag -> tag));
      tags.forEach(tag -> {
        TagModel tagModel = existingTagMap.get(tag.toLowerCase());
        if (tagModel == null) {
          tagModel = new TagModel();
          tagModel.setName(tag);
          tagModel = tagRepository.save(tagModel);
        }
        tagModels.add(tagModel);
      });
      articleModel.setTags(tagModels);
    }
    articleRepository.save(articleModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "{#articleId, #userModel.id}", condition = "#userModel != null")
  public void publishArticle(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.DRAFT || articleStatus == Status.BLOCKED) {
      throw new AccessDeniedException(
          "Publishing not allowed for article with id " + articleId + " and status: "
              + articleStatus);
    }
    if ((articleStatus == Status.MODERATION) && userModel.getRole() != Role.ROLE_ADMIN) {
      throw new AccessDeniedException("Only moderators can publish this article");
    }
    if (articleStatus == Status.HIDDEN && !userModel.getId()
        .equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can publish their hidden article");
    }
    articleModel.setStatus(Status.PUBLISHED);
    articleRepository.save(articleModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "{#articleId, #userModel.id}", condition = "#userModel != null")
  public void hideArticle(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can hide their published article");
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.PUBLISHED) {
      throw new AccessDeniedException("Hiding not allowed for status: " + articleStatus);
    }
    articleModel.setStatus(Status.HIDDEN);
    articleRepository.save(articleModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "#articleId")
  public void blockArticle(Long articleId) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.DRAFT) {
      throw new AccessDeniedException("Blocking not allowed for draft article");
    }
    articleModel.setStatus(Status.BLOCKED);
    articleRepository.save(articleModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "#articleId")
  public void unblockArticle(Long articleId) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.BLOCKED) {
      throw new AccessDeniedException("Unblocking not allowed for this article");
    }
    articleModel.setStatus(Status.DRAFT);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void updateArticle(Long articleId, UpdateArticle updateArticle, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(articleId);
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.DRAFT) {
      throw exceptionFactory.editingNotAllowed(articleId);
    }
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(updateArticle.getCategoryIds()));
    List<Long> compilationIds = updateArticle.getCompilationIds();
    if (compilationIds != null && !compilationIds.isEmpty()) {
      List<CompilationModel> compilations = new ArrayList<>(
          compilationRepository.findAllByIdInAndUser(compilationIds, userModel));
      if (!compilations.isEmpty()) {
        articleModel.setCompilations(compilations);
      }
    }
    List<String> tags = updateArticle.getTags();
    List<TagModel> tagModels = new ArrayList<>();
    if (tags != null && !tags.isEmpty()) {
      List<TagModel> existingTags = tagRepository.findAllByNameIgnoreCaseIn(tags);
      Map<String, TagModel> existingTagMap = existingTags.stream()
          .collect(Collectors.toMap(tag -> tag.getName().toLowerCase().trim(), tag -> tag));
      tags.forEach(tag -> {
        TagModel tagModel = existingTagMap.get(tag.toLowerCase());
        if (tagModel == null) {
          tagModel = new TagModel();
          tagModel.setName(tag);
          tagModel = tagRepository.save(tagModel);
        }
        tagModels.add(tagModel);
      });
    }
    Long originalArticleId = updateArticle.getOriginalArticleId();
    if (originalArticleId != null) {
      articleRepository.findById(originalArticleId)
          .orElseThrow(() -> exceptionFactory.originalArticleNotFound(originalArticleId));
      articleModel.setOriginalArticleId(originalArticleId);
    }
    articleModel.setLanguage(updateArticle.getLanguage());
    articleModel.setStatus(Status.MODERATION);
    articleModel.setTitle(updateArticle.getTitle());
    articleModel.setPreviewContent(updateArticle.getPreviewContent());
    articleModel.setContent(updateArticle.getContent());
    articleModel.setCategories(categories);
    articleModel.setTags(tagModels);
    articleModel.setUser(userModel);
    articleRepository.save(articleModel);
    List<UserModel> moderators = userRepository.findAllByRole(Role.ROLE_ADMIN);
    moderators.forEach(moderator -> {
      NotificationModel notification = NotificationModel.builder()
          .recipient(moderator.getUsername()).articleId(articleId)
          .title(localizationUtil.getLocalizedField("notification.article.moderation.title"))
          .message(localizationUtil.getLocalizedField("notification.article.moderation.message"))
          .type(NotificationType.ARTICLE_MODERATION).isRead(false).build();
      notificationService.saveAndSendToPrivate(moderator.getUsername(), notification);
    });
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "articleById", "viewedArticles",
      "recommendations"}, key = "{#articleId, #userModel.id}", condition = "#userModel != null")
  public void deleteArticle(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (Objects.equals(userModel.getId(), articleModel.getUser().getId())) {
      articleRepository.deleteById(articleId);
    } else {
      throw exceptionFactory.notAllowedToDelete(articleId);
    }
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles",
      "articleById"}, key = "{#articleId, #userModel.id}", condition = "#userModel != null")
  public GetArticle compilate(Long articleId, CompilationsDTO compilations, UserModel userModel) {
    List<Long> compilationIdsToAddOrRemove = compilations.getCompilationIds();
    List<CompilationModel> compilationModelList = compilationRepository.findAllByIdInAndUser(
        compilationIdsToAddOrRemove, userModel);
    if (compilationModelList.size() != compilationIdsToAddOrRemove.size()) {
      throw new IllegalArgumentException("Some compilation IDs are invalid");
    }
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    List<CompilationModel> userCompilations = compilationRepository.findAllByUser(userModel);
    List<CompilationModel> articleCompilations = article.getCompilations();
    Set<Long> compilationIdsSet = new HashSet<>(compilationIdsToAddOrRemove);
    articleCompilations.removeIf(
        compilation -> userCompilations.contains(compilation) && !compilationIdsSet.contains(
            compilation.getId()));
    Set<CompilationModel> currentCompilationsSet = new HashSet<>(articleCompilations);
    for (CompilationModel compilation : compilationModelList) {
      if (!currentCompilationsSet.contains(compilation)) {
        articleCompilations.add(compilation);
      }
    }
    article.setCompilations(articleCompilations);
    return convertArticleModelToDTO(article, userModel);
  }

  private GetArticle convertArticleModelToDTO(ArticleModel articleModel, UserModel userModel) {
    ArticleModel originalArticle =
        articleModel.getOriginalArticleId() != null ? articleRepository.findById(
            articleModel.getOriginalArticleId()).orElse(null) : null;
    boolean likeExists = likeRepository.existsByUserAndArticle(userModel, articleModel);
    List<GetCompilation> compilations = new ArrayList<>(compilationRepository.findAllByIdInAndUser(
        articleModel.getCompilations().stream().map(CompilationModel::getId).toList(),
        userModel)).stream().map(compilation -> GetCompilation.builder().id(compilation.getId())
        .title(compilation.getTitle()).build()).toList();
    Language interfaceLanguage = localizationContext.getLanguage();
    int commentsCount = articleModel.getComments() != null ? articleModel.getComments().size() : 0;
    return GetArticle.builder().id(articleModel.getId()).status(articleModel.getStatus())
        .language(articleModel.getLanguage()).likeCount(articleModel.getLikeCount())
        .originalArticle(originalArticle != null ? GetArticle.builder().id(originalArticle.getId())
            .title(originalArticle.getTitle()).build() : null).title(articleModel.getTitle())
        .isLiked(likeExists).previewContent(articleModel.getPreviewContent())
        .content(articleModel.getContent()).username(articleModel.getUser().getUsername())
        .authorAvatarUrl(articleModel.getUser().getAvatarUrl())
        .categories(articleModel.getCategories().stream().map(category -> {
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
        }).toList()).compilations(compilations).tags(articleModel.getTags().stream()
            .map(tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
            .toList()).compilationsCount(articleModel.getCompilations().size())
        .commentsCount(commentsCount).createdAt(articleModel.getCreatedAt())
        .updatedAt(articleModel.getUpdatedAt()).build();
  }
}
