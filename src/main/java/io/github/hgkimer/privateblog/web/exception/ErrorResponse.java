package io.github.hgkimer.privateblog.web.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp,
    List<FieldErrorResponse> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            LocalDateTime.now(),
            List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
            errorCode.getCode(),
            message,
            LocalDateTime.now(),
            List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode,
        List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            LocalDateTime.now(),
            fieldErrors
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message,
        List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(
            errorCode.getCode(),
            message,
            LocalDateTime.now(),
            fieldErrors
        );
    }

}
