package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateUser;
import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.SubscriptionRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.UserSpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class UserService {

  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final FileUploadUtil fileUploadUtil;
  private final PasswordEncoder passwordEncoder;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  @Cacheable(value = "users", key = "{#order, #sort, #page, #size, #categoryId, #targetUsername, #tag, #info, #role, #isSubscriptions, #isSubscribers, #userModel?.id}", condition = "#info == null || #info.isEmpty()", unless = "#result == null || #result.isEmpty()")
  public Page<GetUser> getUsers(Long categoryId, String targetUsername, UserRole role, String tag,
      String info, int page, int size, String sort, String order, Boolean isSubscriptions,
      Boolean isSubscribers, UserModel userModel) {
    UserModel foundUser = null;
    if (userModel != null) {
      String foundUsername = targetUsername != null ? targetUsername : userModel.getUsername();
      foundUser = userRepository.findByUsername(foundUsername)
          .orElseThrow(() -> exceptionFactory.userNotFound(foundUsername));
    }
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> usersIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      usersIds = searchSession.search(UserModel.class).where(
              f -> f.match().fields("username", "firstName", "lastName", "shortInfo", "bio")
                  .matching(info).fuzzy(1)).fetchHits(searchResultsLimit).stream().map(UserModel::getId)
          .toList();
    }
    Specification<UserModel> spec = UserSpecifications.buildSpecification(categoryId, role, tag,
        usersIds, isSubscriptions, isSubscribers, foundUser);
    return userRepository.findAll(spec, pageable)
        .map(found -> convertUserModelToDto(found, userModel));
  }

  @Transactional
  @Cacheable(value = "userByUsername", key = "{#username, #userModel?.id}")
  public Optional<GetUser> getUserByUsername(String username, UserModel userModel) {
    return userRepository.findByUsername(username)
        .map(foundUser -> convertUserModelToDto(foundUser, userModel));
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "users", allEntries = true),
      @CacheEvict(value = "userByUsername", key = "{#updateUser.username, #userModel.id}")})
  public GetUser updateUser(UpdateUser updateUser, UserModel userModel,
      BindingResult bindingResult) {
    if (updateUser.getUsername() != null && !updateUser.getUsername()
        .equals(userModel.getUsername())) {
      boolean isExistsUser = userRepository.existsByUsername(updateUser.getUsername());
      if (isExistsUser) {
        bindingResult.rejectValue("username", "username.exists",
            localizationUtil.getMessage("user.username.exists"));
      } else {
        userModel.setUsername(updateUser.getUsername());
      }
    }
    if (updateUser.getFirstName() != null) {
      userModel.setFirstName(updateUser.getFirstName());
    }
    if (updateUser.getLastName() != null) {
      userModel.setLastName(updateUser.getLastName());
    }
    if (updateUser.getShortInfo() != null) {
      userModel.setShortInfo(updateUser.getShortInfo());
    }
    if (updateUser.getBio() != null) {
      userModel.setBio(updateUser.getBio());
    }
    if (updateUser.getNewEmail() != null && !updateUser.getNewEmail()
        .equals(userModel.getEmail())) {
      boolean isExistsEmail = userRepository.existsByEmail(updateUser.getNewEmail());
      if (isExistsEmail) {
        bindingResult.rejectValue("newEmail", "email.exists",
            localizationUtil.getMessage("email.exists"));
      } else {
        userModel.setEmail(updateUser.getNewEmail());
      }
    }
    if (updateUser.getNewPassword() != null && !updateUser.getNewPassword().isEmpty()) {
      if (passwordEncoder.matches(updateUser.getCurrentPassword(), userModel.getPassword())) {
        userModel.setPassword(passwordEncoder.encode(updateUser.getNewPassword()));
      } else {
        bindingResult.rejectValue("currentPassword", "password.incorrect",
            localizationUtil.getMessage("user.password.incorrect"));
      }
    }
    Optional.ofNullable(updateUser.getAvatar()).filter(image -> !image.isEmpty())
        .map(fileUploadUtil::uploadUserAvatar).ifPresent(userModel::setAvatarUrl);
    return convertUserModelToDto(userRepository.save(userModel), null);
  }

  @Transactional
  @CacheEvict(value = "users", allEntries = true)
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "users", allEntries = true),
      @CacheEvict(value = "userByUsername", key = "{#targetUsername, #subscriber.id}")})
  public GetUser subscribe(String targetUsername, UserModel subscriber) {
    if (subscriber.getUsername().equals(targetUsername)) {
      throw exceptionFactory.userCannotSubscribeToThemselves();
    }
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> exceptionFactory.targetUserNotFound(targetUsername));
    boolean subscribedExists = subscriptionRepository.existsBySubscriberAndUser(subscriber,
        targetUser);
    if (subscribedExists) {
      throw exceptionFactory.subscriptionAlreadyExists();
    }
    SubscriptionModel subscription = SubscriptionModel.builder().subscriber(subscriber)
        .user(targetUser).build();
    subscriptionRepository.save(subscription);
    return convertUserModelToDto(targetUser, subscriber);
  }

  @Transactional
  @Caching(evict = {@CacheEvict(value = "users", allEntries = true),
      @CacheEvict(value = "userByUsername", key = "{#targetUsername, #subscriber.id}")})
  public GetUser unsubscribe(String targetUsername, UserModel subscriber) {
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> exceptionFactory.targetUserNotFound(targetUsername));
    subscriptionRepository.deleteBySubscriberAndUser(subscriber, targetUser);
    return convertUserModelToDto(targetUser, subscriber);
  }

  private GetUser convertUserModelToDto(UserModel targetUser, UserModel subscriber) {
    List<CategoryModel> categories = categoryRepository.findCategoriesByUserId(targetUser.getId());
    boolean isSubscribed = subscriptionRepository.existsBySubscriberAndUser(subscriber, targetUser);
    Language interfaceLanguage = localizationContext.getLanguage();
    return GetUser.builder().id(targetUser.getId()).username(targetUser.getUsername())
        .isSubscribed(isSubscribed).email(targetUser.getEmail())
        .firstName(targetUser.getFirstName()).lastName(targetUser.getLastName())
        .shortInfo(targetUser.getShortInfo()).bio(targetUser.getBio())
        .avatarUrl(targetUser.getAvatarUrl()).categories(categories.stream().map(category -> {
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
        }).toList()).role(targetUser.getRole()).build();
  }
}