package io.github.hgkimer.privateblog.web.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
