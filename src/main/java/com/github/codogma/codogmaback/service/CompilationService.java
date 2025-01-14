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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
  public List<GetCompilation> getCompilationsByTitle(String name) {
    return compilationRepository.findTop10ByTitleStartingWithIgnoreCase(name).stream()
        .map(this::convertCompilationToDTO).toList();
  }

  @Transactional
  public Optional<GetCompilation> getCompilationById(Long id, UserModel userModel) {
    return compilationRepository.findById(id)
        .map(categoryModel -> convertCompilationToDTO(categoryModel, userModel));
  }

  @Transactional
  public void createCompilation(CreateCompilation createCompilation, UserModel userModel) {
    CompilationModel compilation = CompilationModel.builder().title(createCompilation.getTitle())
        .description(createCompilation.getDescription()).user(userModel).build();
    compilation = compilationRepository.save(compilation);
    MultipartFile image = createCompilation.getImage();
    uploadCompilationImage(image, compilation);
    compilationRepository.save(compilation);
  }

  @Transactional
  public void updateCompilation(Long id, UpdateCompilation updateCompilation, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(id)
        .orElseThrow(() -> new CompilationNotFoundException("Compilation not found"));
    if (!userModel.getId().equals(compilation.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(compilation.getId());
    }
    String title = updateCompilation.getTitle();
    if (title != null && !title.isEmpty()) {
      compilation.setTitle(title);
    }
    String description = updateCompilation.getDescription();
    if (description != null) {
      compilation.setDescription(description);
    }
    MultipartFile image = updateCompilation.getImage();
    uploadCompilationImage(image, compilation);
    compilationRepository.save(compilation);
  }

  @Transactional
  public void deleteCompilation(Long id, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(id)
        .orElseThrow(() -> new CompilationNotFoundException("Compilation not found"));
    if (!userModel.getId().equals(compilation.getUser().getId())) {
      throw exceptionFactory.notAllowedToEdit(compilation.getId());
    }
    compilation.getArticles().forEach(article -> article.getCompilations().remove(compilation));
    compilationRepository.delete(compilation);
  }

  @Transactional
  public GetCompilation bookmark(Long articleId, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.compilationNotFound(articleId));
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
  public GetCompilation unbookmark(Long articleId, UserModel userModel) {
    CompilationModel compilation = compilationRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.compilationNotFound(articleId));
    bookmarkRepository.deleteByUserAndCompilation(userModel, compilation);
    return convertCompilationToDTO(compilation, userModel);
  }

  private void uploadCompilationImage(MultipartFile image, CompilationModel compilation) {
    if (image != null && !image.isEmpty()) {
      String imageUrl = fileUploadUtil.uploadCompilationAvatar(image, compilation.getId());
      compilation.setImageUrl(imageUrl);
    }
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
