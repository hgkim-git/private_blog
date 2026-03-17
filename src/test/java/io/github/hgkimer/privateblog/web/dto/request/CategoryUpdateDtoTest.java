package io.github.hgkimer.privateblog.web.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryUpdateDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  // name, slug 는 CategoryCreateDto와 동일한 유효성 검사이므로 생략

  @Test
  void givenValidDto_whenValidate_thenNoViolation() {
    CategoryUpdateDto validDto = new CategoryUpdateDto("test", "test-slug");
    Set<ConstraintViolation<CategoryUpdateDto>> violations = validator.validate(validDto);
    assertThat(violations).isEmpty();
  }


  @Test
  void givenNullDisplayOrder_whenValidate_thenNoViolation() {
    CategoryUpdateDto validDto = new CategoryUpdateDto("test", "test-slug");
    Set<ConstraintViolation<CategoryUpdateDto>> violations = validator.validate(validDto);
    assertThat(violations).isNotEmpty().hasSize(1).map(ConstraintViolation::getMessage)
        .contains("Display order cannot be null.");
  }

  @Test
  void givenNegativeDisplayOrder_whenValidate_thenNoViolation() {
    CategoryUpdateDto validDto = new CategoryUpdateDto("test", "test-slug");
    Set<ConstraintViolation<CategoryUpdateDto>> violations = validator.validate(validDto);
    assertThat(violations).isNotEmpty().hasSize(1).map(ConstraintViolation::getMessage)
        .contains("Display order must be positive or zero.");
  }
}