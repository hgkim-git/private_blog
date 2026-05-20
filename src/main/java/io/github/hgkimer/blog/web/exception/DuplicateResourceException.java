package io.github.hgkimer.blog.web.exception;

public class DuplicateResourceException extends BusinessException {

  public DuplicateResourceException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DuplicateResourceException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

}
