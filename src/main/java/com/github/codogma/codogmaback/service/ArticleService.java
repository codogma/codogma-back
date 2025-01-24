package com.github.codogma.codogmaback.service;

import static com.github.codogma.codogmaback.util.ContentUtil.createHtmlPreview;

import com.github.codogma.codogmaback.dto.CompilationsDTO;
import com.github.codogma.codogmaback.dto.CreateDraftArticle;
import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.UpdateArticle;
import com.github.codogma.codogmaback.dto.UpdateDraftArticle;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.ArticleModel;
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
import com.github.codogma.codogmaback.repository.NotificationRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.ArticleSpecifications;
import com.github.codogma.codogmaback.repository.specifications.ArticleViewSpecifications;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
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

  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final LikeRepository likeRepository;
  private final ArticleViewRepository articleViewRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CompilationRepository compilationRepository;
  private final LocalizationContext localizationContext;
  private final NotificationRepository notificationRepository;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
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
    return articleRepository.findAll(spec, pageable).map(this::convertArticleModelToDTO)
        .map(this::preparePreview);
  }

  @Transactional(readOnly = true)
  public Page<GetArticle> getViewedArticles(String order, String sort, int page, int size,
      String tag, String content, UserModel userModel) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> articleIds = getArticleIds(content);
    Specification<ArticleView> spec = ArticleViewSpecifications.buildSpecification(tag, articleIds,
        userModel);
    Page<ArticleView> views = articleViewRepository.findAll(spec, pageable);
    return views.map(view -> convertArticleModelToDTO(view.getArticle())).map(this::preparePreview);
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
    if (article.getPreviewContent().isEmpty()) {
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
        .map(this::convertArticleModelToDTO).toList();
  }

  @Transactional
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
    return convertArticleModelToDTO(articleModel);
  }

  @Transactional
  public void toggleLike(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    Optional<LikeModel> existingLike = likeRepository.findByArticleAndUser(article, userModel);
    if (existingLike.isPresent()) {
      likeRepository.delete(existingLike.get());
      article.setLikeCount(article.getLikeCount() - 1);
    } else {
      LikeModel like = LikeModel.builder().article(article).user(userModel).build();
      likeRepository.save(like);
      article.setLikeCount(article.getLikeCount() + 1);
    }
    articleRepository.save(article);
  }

  @Transactional
  public GetArticle recordView(Long articleId, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
//    ArticleView existingView = articleViewRepository.findByUserAndArticle(userModel, articleModel)
//        .orElseGet(() -> ArticleView.builder().user(userModel).article(articleModel).build());
//    existingView.setUpdatedAt(new Date());
//    articleViewRepository.save(existingView);
    return convertArticleModelToDTO(articleModel);
  }

  @Transactional
  public List<GetArticle> getRecommendationsForArticle(Long articleId) {
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    List<Language> supportedLanguages = localizationContext.getSupportedLanguages();
    List<String> categoryNames = article.getCategories().stream()
        .flatMap(category -> category.getName().values().stream()).toList();
    List<String> tagNames = article.getTags().stream().map(TagModel::getName).toList();
    SearchSession searchSession = Search.session(entityManager);
    List<ArticleModel> recommendedArticles = searchSession.search(ArticleModel.class).where(f -> {
      BooleanPredicateClausesStep<?> boolQuery = f.bool()
          .must(f.match().field("status").matching(Status.PUBLISHED))
          .must(f.terms().field("language").matchingAny(supportedLanguages));
      if (!categoryNames.isEmpty()) {
        boolQuery.should(f.terms().fields("categories.name").matchingAny(categoryNames));
      }
      if (!tagNames.isEmpty()) {
        boolQuery.should(f.terms().fields("tags.name").matchingAny(tagNames));
      }
      boolQuery.should(f.match().field("content").matching(article.getContent()))
          .should(f.match().field("title").matching(article.getTitle()));

      return boolQuery;
    }).fetchHits(5);
    return recommendedArticles.stream().map(this::convertArticleModelToDTO).toList();
  }

  @Transactional
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
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public GetArticle createDraftArticle(CreateDraftArticle draftArticle, UserModel userModel) {
    ArticleModel articleModel = ArticleModel.builder().user(userModel)
        .title(draftArticle.getTitle()).content(draftArticle.getContent()).likeCount(0).build();
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
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
          .recipient(moderator.getUsername()).articleId(articleId).title("Article moderation")
          .message("The article submitted for moderation").type(NotificationType.ARTICLE_MODERATION)
          .isRead(false).build();
      notificationRepository.save(notification);
    });
  }

  @Transactional
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
  public GetArticle compilate(Long articleId, CompilationsDTO compilations) {
    List<CompilationModel> compilationModelList = compilationRepository.findAllById(
        compilations.getCompilationIds());
    ArticleModel article = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));
    article.setCompilations(compilationModelList);
    ArticleModel savedArticle = articleRepository.save(article);
    return convertArticleModelToDTO(savedArticle);
  }

  private GetArticle convertArticleModelToDTO(ArticleModel articleModel) {
    ArticleModel originalArticle =
        articleModel.getOriginalArticleId() != null ? articleRepository.findById(
            articleModel.getOriginalArticleId()).orElse(null) : null;
    boolean compilationExists = compilationRepository.existsByArticles_Id(articleModel.getId());
    Language interfaceLanguage = localizationContext.getLocale();
    return GetArticle.builder().id(articleModel.getId()).status(articleModel.getStatus())
        .language(articleModel.getLanguage()).likeCount(articleModel.getLikeCount())
        .originalArticle(originalArticle != null ? GetArticle.builder().id(originalArticle.getId())
            .title(originalArticle.getTitle()).build() : null).title(articleModel.getTitle())
        .isCompilated(compilationExists).previewContent(articleModel.getPreviewContent())
        .content(articleModel.getContent()).username(articleModel.getUser().getUsername())
        .authorAvatarUrl(articleModel.getUser().getAvatarUrl())
        .categories(articleModel.getCategories().stream().map(category -> {
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
        }).toList()).compilations(articleModel.getCompilations().stream().map(
            compilation -> GetCompilation.builder().id(compilation.getId())
                .title(compilation.getTitle()).build()).toList()).tags(
            articleModel.getTags().stream().map(
                    tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
                .toList()).compilationsCount(articleModel.getCompilations().size())
        .commentsCount(articleModel.getComments().size()).createdAt(articleModel.getCreatedAt())
        .updatedAt(articleModel.getUpdatedAt()).build();
  }
}
