package io.github.hgkimer.privateblog.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // Common errors
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", HttpStatus.BAD_REQUEST.getReasonPhrase()),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002",
      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003",
      HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()),

  // Post errors
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Post not found"),
  DUPLICATE_POST_SLUG(HttpStatus.CONFLICT, "P002", "Post slug already exists"),

  // Category errors
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Category not found"),
  DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "CT002", "Category name already exists"),
  DUPLICATE_CATEGORY_SLUG(HttpStatus.CONFLICT, "CT003", "Category slug already exists"),

  // Tag errors
  TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "Tag not found"),
  DUPLICATE_TAG_NAME(HttpStatus.CONFLICT, "T002", "Tag name already exists"),
  DUPLICATE_TAG_SLUG(HttpStatus.CONFLICT, "T003", "Tag slug already exists"),

  // User errors
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
  BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U002", "Invalid username or password"),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "U003", "Authentication required");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
