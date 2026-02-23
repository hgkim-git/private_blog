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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(annotations = {Controller.class})
public class GlobalViewExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  protected String handleBusinessException(BusinessException e, Model model) {
    log.error("Business exception occurred: [{}] {}", e.getErrorCode(), e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), e.getMessage());
    model.addAttribute("error", errorResponse);
    return "error/" + e.getErrorCode().getHttpStatus().value();
  }

  // @RequestBody @Valid 에서 validation을 통과하지 못한 경우
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected String handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, Model model) {
    log.error("Validation error occurred: {}", e.getMessage(), e);
    List<FieldErrorResponse> fieldErrorResponses = e.getFieldErrors().stream()
        .map(FieldErrorResponse::from)
        .toList();
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT,
        fieldErrorResponses);
    model.addAttribute("error", errorResponse);
    return "error/" + HttpStatus.BAD_REQUEST.value();
  }

  // @Validated + @PathVariable/@RequestParam
  @ExceptionHandler(ConstraintViolationException.class)
  protected String handleConstraintViolationException(
      ConstraintViolationException e, Model model) {
    log.error("Validation error occurred: {}", e.getMessage(), e);
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    List<FieldErrorResponse> fieldErrorResponses = violations.stream()
        .map(FieldErrorResponse::from)
        .toList();
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT,
        fieldErrorResponses);
    model.addAttribute("error", errorResponse);
    return "error/" + HttpStatus.BAD_REQUEST.value();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  protected String handleIllegalArgumentException(
      IllegalArgumentException e, Model model) {
    log.error("Invalid input occurred: {}", e.getMessage(), e);
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage());
    model.addAttribute("error", errorResponse);
    return "error/" + HttpStatus.BAD_REQUEST.value();
  }

  @ExceptionHandler(Exception.class)
  protected String handleException(Exception e, Model model) {
    log.error("Unhandled exception occurred", e);
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    model.addAttribute("error", errorResponse);
    return "error/" + HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

}
