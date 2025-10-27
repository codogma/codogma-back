package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateCategoryDTO;
import com.github.codogma.codogmaback.dto.GetCategoryDTO;
import com.github.codogma.codogmaback.dto.GetCategoryToUpdateDTO;
import com.github.codogma.codogmaback.dto.GetImageDTO;
import com.github.codogma.codogmaback.dto.GetImageWithPalette;
import com.github.codogma.codogmaback.dto.GetTagDTO;
import com.github.codogma.codogmaback.dto.PaletteDTO;
import com.github.codogma.codogmaback.dto.SwatchDTO;
import com.github.codogma.codogmaback.dto.UpdateCategoryDTO;
import com.github.codogma.codogmaback.exception.CategoryNotFoundException;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.exception.FavoriteAlreadyExistsException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryImageModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.Palette;
import com.github.codogma.codogmaback.model.Swatch;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryImageRepository;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.FavoriteRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.CategorySpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final UserRepository userRepository;
  private final CategoryImageRepository categoryImageRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;
  private final FavoriteRepository favoriteRepository;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  @Cacheable(value = "categories", key = "{#order, #sort, #page, #size, #tag, #info, #isFavorite, #userModel?.id, @localizationContext.language.code}", condition = "#info == null || #info.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetCategoryDTO> getCategories(String order, String sort, int page, int size,
      String tag,
      String info, Boolean isFavorite, UserModel userModel) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> exceptionFactory.userNotFound(userModel.getUsername())) : null;
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> categoryIds = getCategoryIds(info);
    Specification<CategoryModel> spec = CategorySpecifications.buildSpecification(tag, categoryIds,
        isFavorite, foundUser);
    return categoryRepository.findAll(spec, pageable)
        .map(categoryModel -> convertCategoryToDTO(categoryModel, userModel));
  }

  private List<Long> getCategoryIds(String info) {
    List<Long> categoryIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      categoryIds = searchSession.search(CategoryModel.class)
          .where(f -> f.match().fields("name", "description").matching(info).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(CategoryModel::getId).toList();
    }
    return categoryIds;
  }

  @Transactional
  @Cacheable(value = "categoriesByName", key = "{#name, @localizationContext.language.code}")
  public List<GetCategoryDTO> getCategoriesByNameContaining(String name) {
    Language interfaceLanguage = localizationContext.getLanguage();
    return categoryRepository.findTop10ByNameStartingWithIgnoreCase(interfaceLanguage.name(), name)
        .stream().map(this::convertCategoryToDTO).toList();
  }

  @Transactional
  @Cacheable(value = "categoryById", key = "{#categoryId, #userModel?.id, @localizationContext.language.code}")
  public GetCategoryDTO getCategoryById(Long categoryId, UserModel userModel) {
    CategoryModel categoryModel = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    return convertCategoryToDTO(categoryModel, userModel);
  }

  @Transactional
  public Optional<GetCategoryToUpdateDTO> getCategoryByIdToUpdate(Long categoryId) {
    CategoryImageModel categoryIcon = categoryImageRepository.findByCategoryIdAndIsIconIsTrue(
        categoryId).orElse(null);
    GetImageDTO icon = categoryIcon == null ? null
        : GetImageDTO.builder().imageUrl(categoryIcon.getImageUrl())
            .filename(categoryIcon.getFilename()).build();
    CategoryImageModel categoryImage = categoryImageRepository.findByCategoryIdAndIsFullIsTrue(
        categoryId).orElse(null);
    Palette palette = categoryImage == null ? null : categoryImage.getPalette();
    SwatchDTO vibrant = palette == null ? null : buildSwatchDTO(palette.getVibrant());
    SwatchDTO muted = palette == null ? null : buildSwatchDTO(palette.getMuted());
    SwatchDTO darkVibrant = palette == null ? null : buildSwatchDTO(palette.getDarkVibrant());
    SwatchDTO darkMuted = palette == null ? null : buildSwatchDTO(palette.getDarkMuted());
    SwatchDTO lightVibrant = palette == null ? null : buildSwatchDTO(palette.getLightVibrant());
    SwatchDTO lightMuted = palette == null ? null : buildSwatchDTO(palette.getLightMuted());
    PaletteDTO paletteDTO = palette == null ? null
        : PaletteDTO.builder().vibrant(vibrant).muted(muted).darkVibrant(darkVibrant)
            .darkMuted(darkMuted).lightVibrant(lightVibrant).lightMuted(lightMuted).build();
    GetImageWithPalette image = categoryImage == null ? null
        : GetImageWithPalette.builder().imageUrl(categoryImage.getImageUrl())
            .filename(categoryImage.getFilename()).palette(paletteDTO).build();
    return categoryRepository.findByIdWithCollections(categoryId).map(
        categoryModel -> GetCategoryToUpdateDTO.builder().name(categoryModel.getName()).icon(icon)
            .image(image).description(categoryModel.getDescription()).build());
  }

  @Transactional
  @CacheEvict(cacheNames = {"categories", "categoriesByName"}, allEntries = true)
  public GetCategoryDTO createCategory(CreateCategoryDTO createCategory, UserModel userModel) {
    CategoryModel category = CategoryModel.builder().name(createCategory.getName())
        .description(createCategory.getDescription()).build();
    Optional.ofNullable(createCategory.getIcon()).filter(icon -> !icon.isEmpty())
        .map(fileUploadUtil::uploadCategoryImage).ifPresent(urlPath -> {
          CategoryImageModel categoryImage = CategoryImageModel.builder().category(category)
              .isIcon(true).imageUrl(urlPath).build();
          category.getImages().add(categoryImage);
        });
    PaletteDTO paletteDTO = createCategory.getPalette();
    Palette palette = mapPaletteDTO(paletteDTO);
    Optional.ofNullable(createCategory.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryImage).ifPresent(urlPath -> {
          CategoryImageModel categoryImage = CategoryImageModel.builder().category(category)
              .isFull(true).imageUrl(urlPath).palette(palette).build();
          category.getImages().add(categoryImage);
        });
    CategoryModel savedCategory = categoryRepository.save(category);
    return convertCategoryToDTO(savedCategory, userModel);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(cacheNames = {"categories", "categoriesByName", "users"}, allEntries = true),
      @CacheEvict(value = "categoryById", key = "{#categoryId, #userModel.id, @localizationContext.language.code}")})
  public GetCategoryDTO updateCategory(Long categoryId, UpdateCategoryDTO updateCategory,
      UserModel userModel) {
    CategoryModel category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    Optional.ofNullable(updateCategory.getName()).ifPresent(category::setName);
    Optional.ofNullable(updateCategory.getDescription()).ifPresent(category::setDescription);
    Optional.ofNullable(updateCategory.getIcon()).filter(icon -> !icon.isEmpty())
        .map(fileUploadUtil::uploadCategoryImage).ifPresent(
            urlPath -> categoryImageRepository.findByCategoryIdAndIsIconIsTrue(categoryId)
                .ifPresentOrElse(categoryImage -> {
                  categoryImage.setIcon(true);
                  categoryImage.setImageUrl(urlPath);
                }, () -> {
                  CategoryImageModel categoryImage = CategoryImageModel.builder().category(category)
                      .isIcon(true).imageUrl(urlPath).build();
                  category.getImages().add(categoryImage);
                }));
    PaletteDTO paletteDTO = updateCategory.getPalette();
    Palette palette = mapPaletteDTO(paletteDTO);
    Optional.ofNullable(updateCategory.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCategoryImage).ifPresent(
            urlPath -> categoryImageRepository.findByCategoryIdAndIsFullIsTrue(categoryId)
                .ifPresentOrElse(categoryImage -> {
                  categoryImage.setFull(true);
                  categoryImage.setImageUrl(urlPath);
                  categoryImage.setPalette(palette);
                }, () -> {
                  CategoryImageModel categoryImage = CategoryImageModel.builder().category(category)
                      .isFull(true).imageUrl(urlPath).palette(palette).build();
                  category.getImages().add(categoryImage);
                }));
    categoryRepository.save(category);
    return convertCategoryToDTO(category, userModel);
  }

  @Transactional
  @CacheEvict(cacheNames = {"categories", "categoriesByName"}, allEntries = true)
  public void deleteCategory(Long categoryId) {
    CategoryModel category = categoryRepository.findById(categoryId)
        .orElseThrow(EntityNotFoundException::new);
    category.getArticles().forEach(article -> article.getCategories().remove(category));
    categoryRepository.delete(category);
  }

  @Transactional
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "categories"}, allEntries = true),
      @CacheEvict(value = "categoryById", key = "{#categoryId, #userModel.id, @localizationContext.language.code}")})
  public GetCategoryDTO addToFavorite(Long categoryId, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(categoryId)
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
  @Caching(evict = {@CacheEvict(cacheNames = {"articles", "categories"}, allEntries = true),
      @CacheEvict(value = "categoryById", key = "{#categoryId, #userModel.id, @localizationContext.language.code}")})
  public GetCategoryDTO unfavorite(Long categoryId, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    favoriteRepository.deleteByUserAndCategory(userModel, category);
    return convertCategoryToDTO(category, userModel);
  }

  private GetCategoryDTO convertCategoryToDTO(CategoryModel category, UserModel userModel) {
    List<TagModel> topTags = tagRepository.findTop10TagsByCategoryId(category.getId());
    boolean existed = favoriteRepository.existsByUserAndCategory(userModel, category);
    String localizedCategoryName = localizationUtil.getLocalizedValue(category.getName());
    String localizedCategoryDescription = localizationUtil.getLocalizedValue(
        category.getDescription());
    CategoryImageModel categoryIcon = categoryImageRepository.findByCategoryIdAndIsIconIsTrue(
        category.getId()).orElse(null);
    GetImageDTO icon = categoryIcon == null ? null
        : GetImageDTO.builder().imageUrl(categoryIcon.getImageUrl())
            .filename(categoryIcon.getFilename()).build();
    CategoryImageModel categoryImage = categoryImageRepository.findByCategoryIdAndIsFullIsTrue(
        category.getId()).orElse(null);
    Palette palette = categoryImage == null ? null : categoryImage.getPalette();
    SwatchDTO vibrant = palette == null ? null : buildSwatchDTO(palette.getVibrant());
    SwatchDTO muted = palette == null ? null : buildSwatchDTO(palette.getMuted());
    SwatchDTO darkVibrant = palette == null ? null : buildSwatchDTO(palette.getDarkVibrant());
    SwatchDTO darkMuted = palette == null ? null : buildSwatchDTO(palette.getDarkMuted());
    SwatchDTO lightVibrant = palette == null ? null : buildSwatchDTO(palette.getLightVibrant());
    SwatchDTO lightMuted = palette == null ? null : buildSwatchDTO(palette.getLightMuted());
    PaletteDTO paletteDTO = palette == null ? null
        : PaletteDTO.builder().vibrant(vibrant).muted(muted).darkVibrant(darkVibrant)
            .darkMuted(darkMuted).lightVibrant(lightVibrant).lightMuted(lightMuted).build();
    GetImageWithPalette image = categoryImage == null ? null
        : GetImageWithPalette.builder().imageUrl(categoryImage.getImageUrl())
            .filename(categoryImage.getFilename()).palette(paletteDTO).build();
    return GetCategoryDTO.builder().id(category.getId()).name(localizedCategoryName)
        .isFavorite(existed).description(localizedCategoryDescription).icon(icon).image(image).tags(
            topTags.stream().map(
                    tagModel -> GetTagDTO.builder().id(tagModel.getId()).name(tagModel.getName())
                        .build())
                .toList()).build();
  }

  private GetCategoryDTO convertCategoryToDTO(CategoryModel category) {
    String localizedCategoryName = localizationUtil.getLocalizedValue(category.getName());
    return GetCategoryDTO.builder().id(category.getId()).name(localizedCategoryName).build();
  }

  private SwatchDTO buildSwatchDTO(Swatch swatch) {
    return SwatchDTO.builder().r(swatch.getR()).g(swatch.getG()).b(swatch.getB()).h(swatch.getH())
        .s(swatch.getS()).l(swatch.getL()).hex(swatch.getHex())
        .titleTextColor(swatch.getTitleTextColor()).bodyTextColor(swatch.getBodyTextColor())
        .build();
  }

  private Palette mapPaletteDTO(PaletteDTO dto) {
    return Palette.builder().vibrant(mapSwatch(dto.getVibrant())).muted(mapSwatch(dto.getMuted()))
        .darkVibrant(mapSwatch(dto.getDarkVibrant())).darkMuted(mapSwatch(dto.getDarkMuted()))
        .lightVibrant(mapSwatch(dto.getLightVibrant())).lightMuted(mapSwatch(dto.getLightMuted()))
        .build();
  }

  private Swatch mapSwatch(SwatchDTO swatchDTO) {
    if (swatchDTO == null) {
      return null;
    }
    return Swatch.builder().r(swatchDTO.getR()).g(swatchDTO.getG()).b(swatchDTO.getB())
        .h(swatchDTO.getH()).s(swatchDTO.getS()).l(swatchDTO.getL()).hex(swatchDTO.getHex())
        .titleTextColor(swatchDTO.getTitleTextColor()).bodyTextColor(swatchDTO.getBodyTextColor())
        .build();
  }
}