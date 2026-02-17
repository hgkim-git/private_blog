package io.github.hgkimer.privateblog.web.exception;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidPasswordException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
