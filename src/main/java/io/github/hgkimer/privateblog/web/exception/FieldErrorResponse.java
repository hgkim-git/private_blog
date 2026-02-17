package io.github.hgkimer.privateblog.web.exception;

import jakarta.validation.ConstraintViolation;
import org.springframework.validation.FieldError;

public record FieldErrorResponse(
    String field,
    Object rejectedValue,
    String message
) {

    public static FieldErrorResponse from(FieldError fieldError) {
        return new FieldErrorResponse(
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage()
        );
    }

    public static FieldErrorResponse from(ConstraintViolation<?> violation) {
        return new FieldErrorResponse(
            violation.getPropertyPath().toString(),
            violation.getInvalidValue(),
            violation.getMessage()
        );
    }

}
