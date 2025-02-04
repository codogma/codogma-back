package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateCategory;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetCategoryToUpdate;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.UpdateCategory;
import com.github.codogma.codogmaback.exception.CategoryNotFoundException;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.exception.FavoriteAlreadyExistsException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.FavoriteRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.CategorySpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;
  private final FavoriteRepository favoriteRepository;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public Page<GetCategory> getCategories(String order, String sort, int page, int size, String tag,
      String info, Boolean isFavorite, UserModel userModel) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> exceptionFactory.userNotFound(userModel.getUsername())) : null;
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> categoryIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      categoryIds = searchSession.search(CategoryModel.class)
          .where(f -> f.match().fields("name", "description").matching(info).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(CategoryModel::getId).toList();
    }
    Specification<CategoryModel> spec = CategorySpecifications.buildSpecification(tag, categoryIds,
        isFavorite, foundUser);
    return categoryRepository.findAll(spec, pageable)
        .map(categoryModel -> convertCategoryToDTO(categoryModel, userModel));
  }

  @Transactional
  public List<GetCategory> getCategoriesByNameContaining(String name) {
    Language interfaceLanguage = localizationContext.getLanguage();
    return categoryRepository.findTop10ByNameStartingWithIgnoreCase(interfaceLanguage.name(), name)
        .stream().map(this::convertCategoryToDTO).toList();
  }

  @Transactional
  public Optional<GetCategory> getCategoryById(Long id, UserModel userModel) {
    return categoryRepository.findById(id)
        .map(categoryModel -> convertCategoryToDTO(categoryModel, userModel));
  }

  @Transactional
  public Optional<GetCategoryToUpdate> getCategoryByIdToUpdate(Long id) {
    return categoryRepository.findByIdWithCollections(id).map(
        categoryModel -> GetCategoryToUpdate.builder().name(categoryModel.getName())
            .imageUrl(categoryModel.getImageUrl()).description(categoryModel.getDescription())
            .build());
  }

  @Transactional
  public GetCategory createCategory(CreateCategory createCategory, UserModel userModel) {
    CategoryModel category = CategoryModel.builder().name(createCategory.getName())
        .description(createCategory.getDescription()).build();
    Optional.ofNullable(createCategory.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryAvatar).ifPresent(category::setImageUrl);
    category = categoryRepository.save(category);
    return convertCategoryToDTO(category, userModel);
  }

  @Transactional
  public void updateCategory(Long id, UpdateCategory updateCategory) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    Optional.ofNullable(updateCategory.getName()).ifPresent(category::setName);
    Optional.ofNullable(updateCategory.getDescription()).ifPresent(category::setDescription);
    Optional.ofNullable(updateCategory.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryAvatar).ifPresent(category::setImageUrl);
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(EntityNotFoundException::new);
    category.getArticles().forEach(article -> article.getCategories().remove(category));
    categoryRepository.delete(category);
  }

  @Transactional
  public GetCategory addToFavorite(Long id, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    boolean favoriteExists = favoriteRepository.existsByUserAndCategory(userModel, category);
    if (favoriteExists) {
      throw new FavoriteAlreadyExistsException("Category is already in favorites");
    }
    FavoriteModel favorite = FavoriteModel.builder().user(userModel).category(category).build();
    favoriteRepository.save(favorite);
    return convertCategoryToDTO(category, userModel);
  }

  @Transactional
  public GetCategory unfavorite(Long id, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    favoriteRepository.deleteByUserAndCategory(userModel, category);
    return convertCategoryToDTO(category, userModel);
  }

  private GetCategory convertCategoryToDTO(CategoryModel category, UserModel userModel) {
    List<TagModel> topTags = tagRepository.findTop10TagsByCategoryId(category.getId());
    boolean existed = favoriteRepository.existsByUserAndCategory(userModel, category);
    String localizedCategoryName = localizationUtil.getLocalizedValue(category.getName());
    String localizedCategoryDescription = localizationUtil.getLocalizedValue(
        category.getDescription());
    return GetCategory.builder().id(category.getId()).name(localizedCategoryName)
        .isFavorite(existed).description(localizedCategoryDescription)
        .imageUrl(category.getImageUrl()).tags(topTags.stream()
            .map(tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
            .toList()).build();
  }

  private GetCategory convertCategoryToDTO(CategoryModel category) {
    String localizedCategoryName = localizationUtil.getLocalizedValue(category.getName());
    return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
  }
}
