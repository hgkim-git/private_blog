package io.github.hgkimer.blog.web.exception;

public class InvalidPasswordException extends BusinessException {

  public InvalidPasswordException(ErrorCode errorCode) {
    super(errorCode);
  }

  public InvalidPasswordException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

}
