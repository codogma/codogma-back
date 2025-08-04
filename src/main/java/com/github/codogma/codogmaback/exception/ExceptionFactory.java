package com.github.codogma.codogmaback.exception;

import com.github.codogma.codogmaback.util.LocalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionFactory {

  private final LocalizationUtil localizationUtil;

  public UserAlreadyExistsException userAlreadyExists() {
    return new UserAlreadyExistsException(localizationUtil.getMessage("auth.user.already.exists"));
  }

  public EmailAlreadyConfirmedException emailAlreadyConfirmed() {
    return new EmailAlreadyConfirmedException(
        localizationUtil.getMessage("auth.email.already.confirmed"));
  }

  public InvalidTokenException invalidToken() {
    return new InvalidTokenException(localizationUtil.getMessage("auth.invalid.token"));
  }

  public TokenExpiredException tokenExpired() {
    return new TokenExpiredException(localizationUtil.getMessage("auth.token.expired"));
  }

  public UsernameOrEmailNotFoundException usernameOrEmailNotFound() {
    return new UsernameOrEmailNotFoundException(
        localizationUtil.getMessage("auth.username.or.email.expired"));
  }

  public EmailSendException emailSend() {
    return new EmailSendException(localizationUtil.getMessage("email.send"));
  }

  public EmailNotConfirmedException emailNotConfirmed() {
    return new EmailNotConfirmedException(localizationUtil.getMessage("email.not.confirmed"));
  }

  public SubscriptionAlreadyExistsException subscriptionAlreadyExists() {
    return new SubscriptionAlreadyExistsException(
        localizationUtil.getMessage("user.already.subscribed"));
  }

  public UsernameNotFoundException userNotFound(String username) {
    return new UsernameNotFoundException(
        localizationUtil.getMessage("user.not.found", new Object[]{username}));
  }

  public UsernameNotFoundException targetUserNotFound(String username) {
    return new UsernameNotFoundException(
        localizationUtil.getMessage("user.target.not.found", new Object[]{username}));
  }

  public IllegalArgumentException userCannotSubscribeToThemselves() {
    return new IllegalArgumentException(
        localizationUtil.getMessage("user.cannot.subscribe.to.themselves"));
  }

  public ArticleNotFoundException articleNotFound(Long id) {
    return new ArticleNotFoundException(
        localizationUtil.getMessage("article.not.found", new Object[]{id}));
  }

  public ArticleNotFoundException originalArticleNotFound(Long id) {
    return new ArticleNotFoundException(
        localizationUtil.getMessage("article.original.not.found", new Object[]{id}));
  }

  public AccessDeniedException notAllowedToEdit(Long id) {
    return new AccessDeniedException(
        localizationUtil.getMessage("access.not.allowed.to.edit", new Object[]{id}));
  }

  public AccessDeniedException editingNotAllowed(Long id) {
    return new AccessDeniedException(
        localizationUtil.getMessage("access.editing.not.allowed", new Object[]{id}));
  }

  public AccessDeniedException notAllowedToDelete(Long id) {
    return new AccessDeniedException(
        localizationUtil.getMessage("access.not.allowed.to.delete", new Object[]{id}));
  }

  public CompilationNotFoundException compilationNotFound(Long id) {
    return new CompilationNotFoundException(
        localizationUtil.getMessage("compilation.not.found", new Object[]{id}));
  }
}