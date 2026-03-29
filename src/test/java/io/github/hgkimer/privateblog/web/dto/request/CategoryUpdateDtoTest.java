package io.github.hgkimer.privateblog.web.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryUpdateDtoTest {

  private static ValidatorFactory validatorFactory;
  private Validator validator;

  @BeforeAll
  static void setUpFactory() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
  }

  @AfterAll
  static void tearDownFactory() {
    validatorFactory.close();
  }

  @BeforeEach
  void setUpValidator() {
    validator = validatorFactory.getValidator();
  }

  @Test
  @DisplayName("유효한 카테고리 수정 DTO 검증 테스트")
  void givenValidDto_whenValidate_thenNoViolation() {
    CategoryUpdateDto validDto = new CategoryUpdateDto("test", "test-slug");
    Set<ConstraintViolation<CategoryUpdateDto>> violations = validator.validate(validDto);
    assertThat(violations).isEmpty();
  }

}