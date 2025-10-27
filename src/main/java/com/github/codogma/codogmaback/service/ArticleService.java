package com.github.codogma.codogmaback.service;

import static com.github.codogma.codogmaback.util.ContentUtil.createHtmlPreview;

import com.github.codogma.codogmaback.dto.CompilationsDTO;
import com.github.codogma.codogmaback.dto.CreateDraftArticleDTO;
import com.github.codogma.codogmaback.dto.GetArticleDTO;
import com.github.codogma.codogmaback.dto.GetCategoryDTO;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.GetImageDTO;
import com.github.codogma.codogmaback.dto.GetImageWithPalette;
import com.github.codogma.codogmaback.dto.GetTagDTO;
import com.github.codogma.codogmaback.dto.PaletteDTO;
import com.github.codogma.codogmaback.dto.SwatchDTO;
import com.github.codogma.codogmaback.dto.UpdateArticleDTO;
import com.github.codogma.codogmaback.dto.UpdateDraftArticleDTO;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.exception.LikeAlreadyExistsException;
import com.github.codogma.codogmaback.exception.LikeNotFoundException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.ArticleImageModel;
import com.github.codogma.codogmaback.model.ArticleLikeModel;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.ArticleViewModel;
import com.github.codogma.codogmaback.model.CategoryImageModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.CompilationArticleModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.NotificationModel;
import com.github.codogma.codogmaback.model.NotificationType;
import com.github.codogma.codogmaback.model.Palette;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.Swatch;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.ArticleImageRepository;
import com.github.codogma.codogmaback.repository.ArticleRepository;
import com.github.codogma.codogmaback.repository.ArticleViewRepository;
import com.github.codogma.codogmaback.repository.CategoryImageRepository;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.CompilationArticleRepository;
import com.github.codogma.codogmaback.repository.CompilationRepository;
import com.github.codogma.codogmaback.repository.LikeRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.projection.ArticleScoreProjection;
import com.github.codogma.codogmaback.repository.specifications.ArticleSpecifications;
import com.github.codogma.codogmaback.repository.specifications.ArticleViewSpecifications;
import com.github.codogma.codogmaback.util.KeywordExtractor;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.cache.annotation.Caching;
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
  private final ArticleImageRepository articleImageRepository;
  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final CategoryRepository categoryRepository;
  private final CategoryImageRepository categoryImageRepository;
  private final CompilationRepository compilationRepository;
  private final KeywordExtractor keywordExtractor;
  private final LikeRepository likeRepository;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;
  private final NotificationService notificationService;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final ContentBasedRecommender contentBasedRecommender;
  private final CompilationArticleRepository compilationArticleRepository;
  private final LikeService likeService;

  @Value("${search.results.limit}")
  private int searchResultsLimit;
  @Value("${recommendation.limit:5}")
  private int recommendationLimit;

  @Transactional
  @Cacheable(value = "articles", key = "{#order, #sort, #page, #size, #categoryId, #compilationId, #tag, #username, #isFeed, #content, #userModel?.id, @localizationContext.supportedLanguages}", condition = "#content == null || #content.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetArticleDTO> getArticles(String order, String sort, int page, int size,
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
  @Cacheable(value = "viewedArticles", key = "{#order, #sort, #page, #size, #tag, #content, #userModel?.id, @localizationContext.supportedLanguages}", condition = "#content == null || #content.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetArticleDTO> getViewedArticles(String order, String sort, int page, int size,
      String tag, String content, UserModel userModel) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> articleIds = getArticleIds(content);
    Specification<ArticleViewModel> spec = ArticleViewSpecifications.buildSpecification(tag,
        articleIds, userModel);
    Page<ArticleViewModel> views = articleViewRepository.findAll(spec, pageable);
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

  private GetArticleDTO preparePreview(GetArticleDTO article) {
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
  public List<GetArticleDTO> getDraftArticles(UserModel userModel) {
    return articleRepository.findAllByUserAndStatus(userModel, Status.DRAFT).stream()
        .map(articleModel -> convertArticleModelToDTO(articleModel, userModel)).toList();
  }

  @Transactional
  @Cacheable(value = "articleById", key = "{#articleId, #userModel?.id, @localizationContext.language.code}")
  public GetArticleDTO getArticleById(Long articleId, UserModel userModel) {
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
    boolean likeExists = likeRepository.existsByUserAndArticle(userModel, articleModel);
    GetArticleDTO getArticle = convertArticleModelToDTO(articleModel, userModel);
    getArticle.setIsLiked(likeExists);
    return getArticle;
  }

  @Transactional
  @CacheEvict(value = "articleById", allEntries = true)
  public void like(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Optional<ArticleLikeModel> existingLike = likeRepository.findByArticleAndUser(article,
        userModel);
    if (existingLike.isPresent()) {
      throw new LikeAlreadyExistsException("The article was already liked");
    }
    ArticleLikeModel like = ArticleLikeModel.builder().article(article).user(userModel).build();
    likeRepository.save(like);
    likeService.incrementLikesCount(articleId);
  }

  @Transactional
  @CacheEvict(value = "articleById", allEntries = true)
  public void unlike(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    ArticleLikeModel existingLike = likeRepository.findByArticleAndUser(article, userModel)
        .orElseThrow(() -> new LikeNotFoundException("Like not found"));
    likeRepository.delete(existingLike);
    likeService.decrementLikesCount(articleId);
  }

  @Transactional
  @CacheEvict(value = "viewedArticles", allEntries = true)
  public void recordView(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    if (userModel != null) {
      ArticleViewModel existingView = articleViewRepository.findByUserAndArticle(userModel,
          articleModel).orElseGet(
          () -> ArticleViewModel.builder().user(userModel).article(articleModel).build());
      existingView.setUpdatedAt(Instant.now());
      articleViewRepository.save(existingView);
    }
  }

  @Transactional
  @Cacheable(value = "recommendations", key = "#articleId")
  public List<GetArticleDTO> getRecommendationsForArticle(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Instant sixMonthsAgo = Instant.now().minus(Duration.ofDays(180));
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
              .must(f.range().field("createdAt").atLeast(sixMonthsAgo)).should(
                  f.simpleQueryString().fields("title", "tags.name").matching(combinedKeywords)
                      .defaultOperator(BooleanOperator.OR).boost(1.0f))
              .must(f.match().field("title").matching(article.getTitle()).fuzzy().boost(10.0f))
              .should(f.match().field("content").matching(article.getContent()).boost(0.00002f));
          if (article.getLikesCount() != null) {
            bool.should(
                f.range().field("likesCount").atLeast(Math.round(article.getLikesCount() * 0.8f))
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
          b.add(f.field("likesCount").desc());
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
  public List<GetArticleDTO> getRecommendations(UserModel user) {
    if (user == null) {
      return Collections.emptyList();
    }
    List<ArticleModel> recommendations = contentBasedRecommender.getRecommendations(user.getId());
    return recommendations.stream().map(article -> convertArticleModelToDTO(article, user))
        .map(this::preparePreview).collect(Collectors.toList());
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "viewedArticles", "recommendations",
      "articleById"}, allEntries = true)
  public GetArticleDTO getDraftedArticleById(Long articleId, UserModel userModel) {
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
  @CacheEvict(cacheNames = {"articles", "recommendations"}, allEntries = true)
  public GetArticleDTO createDraftArticle(CreateDraftArticleDTO draftArticle, UserModel userModel) {
    ArticleModel articleModel = ArticleModel.builder().user(userModel)
        .title(draftArticle.getTitle()).likesCount(0).build();
    articleModel = articleRepository.save(articleModel);
    return convertArticleModelToDTO(articleModel, userModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"articles", "viewedArticles", "recommendations",
      "articleById"}, allEntries = true)
  public void updateDraftArticle(Long articleId, UpdateDraftArticleDTO draftArticle,
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
    Optional.ofNullable(draftArticle.getLanguage()).ifPresent(articleModel::setLanguage);
    Optional.ofNullable(draftArticle.getOriginalArticleId()).ifPresent(id -> {
      articleRepository.findById(id)
          .orElseThrow(() -> exceptionFactory.originalArticleNotFound(id));
      articleModel.setOriginalArticleId(id);
    });
    Optional.ofNullable(draftArticle.getTitle()).ifPresent(articleModel::setTitle);
    Optional.ofNullable(draftArticle.getPreviewContent())
        .ifPresent(articleModel::setPreviewContent);
    Optional.ofNullable(draftArticle.getContent()).ifPresent(articleModel::setContent);

    List<Long> categoryIds = draftArticle.getCategoryIds();
    if (categoryIds != null) {
      List<CategoryModel> categories = categoryRepository.findAllById(categoryIds);
      articleModel.setCategories(categories);
    }

    // Обновление подборок с учетом порядка
    List<Long> compilationIds = draftArticle.getCompilationIds();
    if (compilationIds != null) {
      // Получаем существующие связи
      List<CompilationArticleModel> existingLinks = compilationArticleRepository.findByArticleAndCompilationUser(
          articleModel, userModel);
      // Создаем мапу для быстрого поиска существующих связей
      Map<Long, CompilationArticleModel> existingLinksMap = existingLinks.stream().collect(
          Collectors.toMap(compilationArticle -> compilationArticle.getCompilation().getId(),
              compilationArticle -> compilationArticle));
      // Удаляем связи, которые отсутствуют в новом списке
      List<CompilationArticleModel> linksToRemove = existingLinks.stream().filter(
          compilationArticle -> !compilationIds.contains(
              compilationArticle.getCompilation().getId())).toList();

      for (CompilationArticleModel removedLink : linksToRemove) {
        CompilationModel compilation = removedLink.getCompilation();
        compilation.getCompilationArticles().remove(removedLink);
        compilationArticleRepository.delete(removedLink);
        List<CompilationArticleModel> links = compilationArticleRepository.findByCompilationOrderByPosition(
            compilation);
        for (int i = 0; i < links.size(); i++) {
          CompilationArticleModel link = links.get(i);
          link.setPosition(i);
        }
        compilationArticleRepository.saveAll(links);
      }

      // Создаем или обновляем связи
      List<CompilationArticleModel> linksToSave = new ArrayList<>();

      // Для каждой переданной подборки
      for (Long compilationId : compilationIds) {
        CompilationArticleModel link = existingLinksMap.get(compilationId);
        // Получаем актуальную подборку (возможно, её коллекция compilationArticles не загружена)
        CompilationModel compilation = compilationRepository.findById(compilationId)
            .orElseThrow(() -> exceptionFactory.compilationNotFound(compilationId));

        if (link == null) {
          // Новая связь: вычисляем максимальную позицию в подборке и добавляем статью в конец
          int maxPosition = compilation.getCompilationArticles().stream()
              .map(CompilationArticleModel::getPosition).max(Integer::compareTo).orElse(-1);
          link = CompilationArticleModel.builder().article(articleModel).compilation(compilation)
              .position(maxPosition + 1).build();
          // Добавляем связь в коллекцию подборки, чтобы orphanRemoval работал корректно
          compilation.getCompilationArticles().add(link);
        }
        // Если связь уже существует, оставляем её позицию без изменений (или можно обновить, если требуется другая логика)
        linksToSave.add(link);
      }
      compilationArticleRepository.saveAll(linksToSave);
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, #userModel?.id, @localizationContext.language.code}")})
  public void updateArticle(Long articleId, UpdateArticleDTO updateArticle, UserModel userModel) {
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

    // Обновление подборок с учетом порядка
    List<Long> compilationIds = updateArticle.getCompilationIds();
    if (compilationIds != null) {
      // Получаем существующие связи
      List<CompilationArticleModel> existingLinks = compilationArticleRepository.findByArticleAndCompilationUser(
          articleModel, userModel);
      // Создаем мапу для быстрого поиска существующих связей
      Map<Long, CompilationArticleModel> existingLinksMap = existingLinks.stream().collect(
          Collectors.toMap(compilationArticle -> compilationArticle.getCompilation().getId(),
              compilationArticle -> compilationArticle));
      // Удаляем связи, которые отсутствуют в новом списке
      List<CompilationArticleModel> linksToRemove = existingLinks.stream().filter(
          compilationArticle -> !compilationIds.contains(
              compilationArticle.getCompilation().getId())).toList();

      for (CompilationArticleModel removedLink : linksToRemove) {
        CompilationModel compilation = removedLink.getCompilation();
        compilation.getCompilationArticles().remove(removedLink);
        compilationArticleRepository.delete(removedLink);
        List<CompilationArticleModel> links = compilationArticleRepository.findByCompilationOrderByPosition(
            compilation);
        for (int i = 0; i < links.size(); i++) {
          CompilationArticleModel link = links.get(i);
          link.setPosition(i);
        }
        compilationArticleRepository.saveAll(links);
      }

      // Создаем или обновляем связи
      List<CompilationArticleModel> linksToSave = new ArrayList<>();

      // Для каждой переданной подборки
      for (Long compilationId : compilationIds) {
        CompilationArticleModel link = existingLinksMap.get(compilationId);
        // Получаем актуальную подборку (возможно, её коллекция compilationArticles не загружена)
        CompilationModel compilation = compilationRepository.findById(compilationId)
            .orElseThrow(() -> exceptionFactory.compilationNotFound(compilationId));

        if (link == null) {
          // Новая связь: вычисляем максимальную позицию в подборке и добавляем статью в конец
          int maxPosition = compilation.getCompilationArticles().stream()
              .map(CompilationArticleModel::getPosition).max(Integer::compareTo).orElse(-1);
          link = CompilationArticleModel.builder().article(articleModel).compilation(compilation)
              .position(maxPosition + 1).build();
          // Добавляем связь в коллекцию подборки, чтобы orphanRemoval работал корректно
          compilation.getCompilationArticles().add(link);
        }
        // Если связь уже существует, оставляем её позицию без изменений (или можно обновить, если требуется другая логика)
        linksToSave.add(link);
      }
      compilationArticleRepository.saveAll(linksToSave);
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
    Optional.ofNullable(updateArticle.getOriginalArticleId()).ifPresent(id -> {
      articleRepository.findById(id)
          .orElseThrow(() -> exceptionFactory.originalArticleNotFound(id));
      articleModel.setOriginalArticleId(id);
    });
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, #userModel?.id, @localizationContext.language.code}")})
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, #userModel?.id}")})
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, null}")})
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, null}")})
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, #userModel?.id}")})
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "compilations", "viewedArticles",
      "recommendations"}, allEntries = true),
      @CacheEvict(value = "articleById", key = "{#articleId, #userModel?.id}")})
  public void compilate(Long articleId, CompilationsDTO compilations, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    List<Long> compilationIds = compilations.getCompilationIds();
    List<CompilationModel> compilationModelList = compilationRepository.findAllByIdInAndUser(
        compilationIds, userModel);
    if (compilationModelList.size() != compilationIds.size()) {
      throw new IllegalArgumentException("Some compilation IDs are invalid");
    }
    // Получаем существующие связи
    List<CompilationArticleModel> existingLinks = compilationArticleRepository.findByArticleAndCompilationUser(
        articleModel, userModel);
    // Создаем мапу для быстрого поиска существующих связей
    Map<Long, CompilationArticleModel> existingLinksMap = existingLinks.stream().collect(
        Collectors.toMap(compilationArticle -> compilationArticle.getCompilation().getId(),
            compilationArticle -> compilationArticle));
    // Удаляем связи, которые отсутствуют в новом списке
    List<CompilationArticleModel> linksToRemove = existingLinks.stream().filter(
            compilationArticle -> !compilationIds.contains(compilationArticle.getCompilation().getId()))
        .toList();

    for (CompilationArticleModel removedLink : linksToRemove) {
      CompilationModel compilation = removedLink.getCompilation();
      compilation.getCompilationArticles().remove(removedLink);
      compilationArticleRepository.delete(removedLink);
      List<CompilationArticleModel> links = compilationArticleRepository.findByCompilationOrderByPosition(
          compilation);
      for (int i = 0; i < links.size(); i++) {
        CompilationArticleModel link = links.get(i);
        link.setPosition(i);
      }
      compilationArticleRepository.saveAll(links);
    }

    // Создаем или обновляем связи
    List<CompilationArticleModel> linksToSave = new ArrayList<>();

    // Для каждой переданной подборки
    for (Long compilationId : compilationIds) {
      CompilationArticleModel link = existingLinksMap.get(compilationId);
      // Получаем актуальную подборку (возможно, её коллекция compilationArticles не загружена)
      CompilationModel compilation = compilationRepository.findById(compilationId)
          .orElseThrow(() -> exceptionFactory.compilationNotFound(compilationId));

      if (link == null) {
        // Новая связь: вычисляем максимальную позицию в подборке и добавляем статью в конец
        int maxPosition = compilation.getCompilationArticles().stream()
            .map(CompilationArticleModel::getPosition).max(Integer::compareTo).orElse(-1);
        link = CompilationArticleModel.builder().article(articleModel).compilation(compilation)
            .position(maxPosition + 1).build();
        // Добавляем связь в коллекцию подборки, чтобы orphanRemoval работал корректно
        compilation.getCompilationArticles().add(link);
      }
      // Если связь уже существует, оставляем её позицию без изменений (или можно обновить, если требуется другая логика)
      linksToSave.add(link);
    }
    compilationArticleRepository.saveAll(linksToSave);
  }

  private GetArticleDTO convertArticleModelToDTO(ArticleModel articleModel, UserModel userModel) {
    ArticleModel originalArticle =
        articleModel.getOriginalArticleId() != null ? articleRepository.findById(
            articleModel.getOriginalArticleId()).orElse(null) : null;
    List<GetCompilation> compilations = compilationArticleRepository.findAllByArticleAndCompilationUser(
        articleModel, userModel).stream().map(compilationArticle -> GetCompilation.builder()
        .id(compilationArticle.getCompilation().getId())
        .title(compilationArticle.getCompilation().getTitle()).build()).toList();
    Language interfaceLanguage = localizationContext.getLanguage();
    int commentsCount = articleModel.getComments() != null ? articleModel.getComments().size() : 0;
    ArticleImageModel articleImage = articleImageRepository.findByArticleIdAndIsPreviewIsTrue(
        articleModel.getId()).orElse(null);
    Palette palette = articleImage == null ? null : articleImage.getPalette();
    SwatchDTO vibrant = palette == null ? null : buildSwatchDTO(palette.getVibrant());
    SwatchDTO muted = palette == null ? null : buildSwatchDTO(palette.getMuted());
    SwatchDTO darkVibrant = palette == null ? null : buildSwatchDTO(palette.getDarkVibrant());
    SwatchDTO darkMuted = palette == null ? null : buildSwatchDTO(palette.getDarkMuted());
    SwatchDTO lightVibrant = palette == null ? null : buildSwatchDTO(palette.getLightVibrant());
    SwatchDTO lightMuted = palette == null ? null : buildSwatchDTO(palette.getLightMuted());
    PaletteDTO paletteDTO = palette == null ? null
        : PaletteDTO.builder().vibrant(vibrant).muted(muted).darkVibrant(darkVibrant)
            .darkMuted(darkMuted).lightVibrant(lightVibrant).lightMuted(lightMuted).build();
    GetImageWithPalette image = articleImage == null ? null
        : GetImageWithPalette.builder().imageUrl(articleImage.getImageUrl())
            .filename(articleImage.getFilename()).palette(paletteDTO).build();
    return GetArticleDTO.builder().id(articleModel.getId()).status(articleModel.getStatus())
        .language(articleModel.getLanguage()).likesCount(articleModel.getLikesCount())
        .originalArticle(
            originalArticle != null ? GetArticleDTO.builder().id(originalArticle.getId())
                .title(originalArticle.getTitle()).build() : null).title(articleModel.getTitle())
        .image(image).previewContent(articleModel.getPreviewContent())
        .content(articleModel.getContent()).username(articleModel.getUser().getUsername())
        .authorAvatarUrl(articleModel.getUser().getAvatarUrl())
        .categories(articleModel.getCategories().stream().map(category -> {
          CategoryImageModel categoryIcon = categoryImageRepository.findByCategoryIdAndIsIconIsTrue(
              category.getId()).orElse(null);
          GetImageDTO icon = categoryIcon == null ? null
              : GetImageDTO.builder().imageUrl(categoryIcon.getImageUrl())
                  .filename(categoryIcon.getFilename()).build();
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategoryDTO.builder().id(category.getId()).name(localizedCategoryName)
              .icon(icon).build();
        }).toList()).compilations(compilations).tags(articleModel.getTags().stream().map(
                tagModel -> GetTagDTO.builder().id(tagModel.getId()).name(tagModel.getName()).build())
            .toList()).viewsCount(articleModel.getViews().size()).commentsCount(commentsCount)
        .createdAt(articleModel.getCreatedAt()).updatedAt(articleModel.getUpdatedAt()).build();
  }

  private SwatchDTO buildSwatchDTO(Swatch swatch) {
    return SwatchDTO.builder().r(swatch.getR()).g(swatch.getG()).b(swatch.getB()).h(swatch.getH())
        .s(swatch.getS()).l(swatch.getL()).hex(swatch.getHex())
        .titleTextColor(swatch.getTitleTextColor()).bodyTextColor(swatch.getBodyTextColor())
        .build();
  }
}
