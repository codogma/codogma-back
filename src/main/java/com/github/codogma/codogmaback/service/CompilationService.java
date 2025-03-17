package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateCompilation;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.dto.UpdateCompilation;
import com.github.codogma.codogmaback.exception.BookmarkAlreadyExistsException;
import com.github.codogma.codogmaback.exception.CompilationNotFoundException;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.model.BookmarkModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.BookmarkRepository;
import com.github.codogma.codogmaback.repository.CompilationRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.CompilationSpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {

  private final UserRepository userRepository;
  private final ExceptionFactory exceptionFactory;
  private final EntityManager entityManager;
  private final CompilationRepository compilationRepository;
  private final BookmarkRepository bookmarkRepository;
  private final FileUploadUtil fileUploadUtil;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  @Cacheable(value = "compilations", key = "{#order, #sort, #page, #size, #tag, #content, #isBookmarked, #username, #userModel?.id}", unless = "#result == null || #result.isEmpty()")
  public Page<GetCompilation> getCompilations(String tag, String content, String username,
      Boolean isBookmarked, int page, int size, String sort, String order, UserModel userModel) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> exceptionFactory.userNotFound(userModel.getUsername())) : null;
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> compilationIds = null;
    if (content != null && !content.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      compilationIds = searchSession.search(CompilationModel.class)
          .where(f -> f.match().fields("title", "description").matching(content).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(CompilationModel::getId).toList();
    }
    Specification<CompilationModel> spec = CompilationSpecifications.buildSpecification(tag,
        username, isBookmarked, foundUser, compilationIds);
    return compilationRepository.findAll(spec, pageable)
        .map(compilationModel -> convertCompilationToDTO(compilationModel, userModel));
  }

  @Transactional
  @Cacheable(value = "compilationsByTitle", key = "#title")
  public List<GetCompilation> getCompilationsByTitle(String title, UserModel user) {
    return compilationRepository.findTop10ByTitleStartingWithIgnoreCaseAndUser(title, user).stream()
        .map(this::convertCompilationToDTO).toList();
  }

  @Transactional
  @Cacheable(value = "compilationById", key = "{#compilationId, #userModel?.id}")
  public Optional<GetCompilation> getCompilationById(Long compilationId, UserModel userModel) {
    return compilationRepository.findById(compilationId)
        .map(categoryModel -> convertCompilationToDTO(categoryModel, userModel));
  }

  @Transactional
  @CacheEvict(cacheNames = {"compilations", "compilationsByTitle"}, allEntries = true)
  public void createCompilation(CreateCompilation createCompilation, UserModel userModel) {
    CompilationModel compilation = CompilationModel.builder().title(createCompilation.getTitle())
        .description(createCompilation.getDescription()).user(userModel).build();
    Optional.ofNullable(createCompilation.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCompilationAvatar).ifPresent(compilation::setImageUrl);
    compilationRepository.save(compilation);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(cacheNames = {"compilations", "compilationsByTitle"}, allEntries = true),
      @CacheEvict(value = "compilationById", key = "{#compilationId, #userModel.id}")})
  public void updateCompilation(Long compilationId, UpdateCompilation updateCompilation,
      UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(compilationId)
        .orElseThrow(() -> new CompilationNotFoundException("Compilation not found"));
    if (!userModel.getId().equals(compilation.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(compilation.getId());
    }
    Optional.ofNullable(updateCompilation.getTitle()).filter(title -> !title.isEmpty())
        .ifPresent(compilation::setTitle);
    Optional.ofNullable(updateCompilation.getDescription()).ifPresent(compilation::setDescription);
    Optional.ofNullable(updateCompilation.getImage()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadCompilationAvatar).ifPresent(compilation::setImageUrl);
    compilationRepository.save(compilation);
  }

  @Transactional
  @CacheEvict(cacheNames = {"compilations", "compilationsByTitle"}, allEntries = true)
  public void deleteCompilation(Long compilationId, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(compilationId)
        .orElseThrow(() -> new CompilationNotFoundException("Compilation not found"));
    if (!userModel.getId().equals(compilation.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(compilation.getId());
    }
    compilation.getArticles().forEach(article -> article.getCompilations().remove(compilation));
    compilationRepository.delete(compilation);
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "compilations", allEntries = true),
      @CacheEvict(value = "categoryById", key = "{#compilationId, #userModel.id}")})
  public GetCompilation bookmark(Long compilationId, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(compilationId)
        .orElseThrow(() -> exceptionFactory.compilationNotFound(compilationId));
    boolean bookmarkExists = bookmarkRepository.existsByUserAndCompilation(userModel, compilation);
    if (bookmarkExists) {
      throw new BookmarkAlreadyExistsException("Compilation already bookmarked");
    }
    BookmarkModel bookmark = BookmarkModel.builder().user(userModel).compilation(compilation)
        .build();
    bookmarkRepository.save(bookmark);
    return convertCompilationToDTO(compilation, userModel);
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "compilations", allEntries = true),
      @CacheEvict(value = "categoryById", key = "{#compilationId, #userModel.id}")})
  public GetCompilation unbookmark(Long compilationId, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(compilationId)
        .orElseThrow(() -> exceptionFactory.compilationNotFound(compilationId));
    bookmarkRepository.deleteByUserAndCompilation(userModel, compilation);
    return convertCompilationToDTO(compilation, userModel);
  }

  private GetCompilation convertCompilationToDTO(CompilationModel compilation,
      UserModel userModel) {
    boolean existed = bookmarkRepository.existsByUserAndCompilation(userModel, compilation);
    String userFullName =
        compilation.getUser().getFirstName() != null || compilation.getUser().getLastName() != null
            ? compilation.getUser().getFirstName() + " " + compilation.getUser().getLastName()
            : compilation.getUser().getUsername();
    return GetCompilation.builder().id(compilation.getId()).isBookmarked(existed)
        .bookmarksCount(compilation.getBookmarks().size()).title(compilation.getTitle())
        .description(compilation.getDescription()).ownerName(compilation.getUser().getUsername())
        .ownerFullName(userFullName.trim()).ownerAvatarUrl(compilation.getUser().getAvatarUrl())
        .imageUrl(compilation.getImageUrl()).build();
  }

  private GetCompilation convertCompilationToDTO(CompilationModel compilation) {
    return GetCompilation.builder().id(compilation.getId()).title(compilation.getTitle()).build();
  }
}
