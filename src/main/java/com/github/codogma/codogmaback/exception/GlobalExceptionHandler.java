package com.github.codogma.codogmaback.exception;

import com.github.codogma.codogmaback.dto.ErrorResponse;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final LocalizationUtil localizationUtil;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    String message = "Data integrity violation";
    if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException hibernateEx) {
      String constraintName = hibernateEx.getConstraintName();
      String sqlMessage = hibernateEx.getSQLException().getMessage();
      message = "Constraint violation: " + constraintName + ". " + sqlMessage;
    }

    Map<String, String> response = Map.of("error", "DATA_INTEGRITY_VIOLATION", "message", message);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<String> handleUsernameAlreadyExistsException(
      UsernameAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UsernameOrEmailNotFoundException.class)
  public ResponseEntity<String> handleUsernameOrEmailNotFoundException(
      UsernameOrEmailNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(EmailNotFoundException.class)
  public ResponseEntity<String> handleEmailNotFoundException(EmailNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(EmailAlreadyConfirmedException.class)
  public ResponseEntity<String> handleEmailAlreadyConfirmedException(
      EmailAlreadyConfirmedException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailNotConfirmedException.class)
  public ResponseEntity<String> handleEmailNotConfirmedException(EmailNotConfirmedException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailSendException.class)
  public ResponseEntity<String> handleEmailSendException(EmailSendException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IncorrectPasswordException.class)
  public ResponseEntity<String> handleIncorrectPasswordException(IncorrectPasswordException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
    return new ResponseEntity<>(localizationUtil.getMessage("auth.bad.credentials"),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
    log.error("NoResourceFoundException caught", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ArticleNotFoundException.class)
  public ResponseEntity<String> handleArticleNotFoundException(ArticleNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<String> handleCategoryNotFoundException(CategoryNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.error("Authentication exception caught", ex);
    ErrorResponse errorResponse = ErrorResponse.builder().error("Unauthorized")
        .message("Authentication failed. Please check your credentials.").build();
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex) {
    log.error("Authorization denied exception caught", ex);
    ErrorResponse errorResponse = ErrorResponse.builder().error("Forbidden")
        .message("You do not have permission to access this resource.").build();
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<String> handleTokenExpiredException(TokenExpiredException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(FileStorageException.class)
  public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(CommentNotFoundException.class)
  public ResponseEntity<String> handleCommentNotFoundException(CommentNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(SubscriptionAlreadyExistsException.class)
  public ResponseEntity<String> handleSubscribeAlreadyExistsException(
      SubscriptionAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CompilationAlreadyExistsException.class)
  public ResponseEntity<String> handleCompilationAlreadyExistsException(
      CompilationAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CompilationNotExistsException.class)
  public ResponseEntity<String> handleCompilationNotExistsException(
      CompilationNotExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CompilationNotFoundException.class)
  public ResponseEntity<String> handleCompilationNotFoundException(
      CompilationNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FavoriteAlreadyExistsException.class)
  public ResponseEntity<String> handleFavoriteAlreadyExistsException(
      FavoriteAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleAllExceptions(Exception ex) {
    log.error("Unhandled exception occurred: ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<String> handleTypeMismatch(HttpRequestMethodNotSupportedException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(LikeAlreadyExistsException.class)
  public ResponseEntity<String> handleLikeAlreadyExistsException(LikeAlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(LikeNotFoundException.class)
  public ResponseEntity<String> handleLikeNotFoundException(LikeNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserIdNotFoundException.class)
  public ResponseEntity<String> handleUserIdNotFoundException(UserIdNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}