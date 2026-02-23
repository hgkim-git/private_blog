package io.github.hgkimer.privateblog.web.exception.handler;

import io.github.hgkimer.privateblog.web.exception.BusinessException;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import io.github.hgkimer.privateblog.web.exception.FieldErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalApiExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.error("Business exception occurred: [{}] {}", e.getErrorCode(), e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), e.getMessage());
    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(errorResponse);
  }

  // @RequestBody @Valid 에서 validation을 통과하지 못한 경우
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("Validation error occurred: {}", e.getMessage(), e);
    List<FieldErrorResponse> fieldErrorResponses = e.getFieldErrors().stream()
        .map(FieldErrorResponse::from)
        .toList();
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT,
        fieldErrorResponses);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  // @Validated + @PathVariable/@RequestParam
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException e) {
    log.error("Validation error occurred: {}", e.getMessage(), e);
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    List<FieldErrorResponse> fieldErrorResponses = violations.stream()
        .map(FieldErrorResponse::from)
        .toList();
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT,
        fieldErrorResponses);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException e) {
    log.error("Invalid input occurred: {}", e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled exception occurred", e);
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    return ResponseEntity.internalServerError().body(errorResponse);
  }

}
