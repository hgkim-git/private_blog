package io.github.hgkimer.privateblog.web.dto.request;


import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryCreateDtoTest {

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
  @DisplayName("유효한 카테고리 생성 DTO 검증 테스트")
  void givenValidDto_whenCreateDto_thenSuccess() {
    CategoryCreateDto dto = new CategoryCreateDto("test", "test");
    assertThat(dto).isNotNull();
    Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("이름이 비어있는 카테고리 생성 DTO 검증 시 제약 조건 위반이 발생해야 한다.")
  void givenInvalidName_whenCreateDto_thenHaveViolation() {
    CategoryCreateDto dto = new CategoryCreateDto("", "test");
    Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isNotEmpty().hasSize(1);
    assertThat(violations).
        allSatisfy(
            violation -> assertThat(violation.getMessage()).isEqualTo(
                "Category name cannot be empty."));
  }

  @Test
  @DisplayName("유효하지 않은 슬러그를 가진 카테고리 생성 DTO 검증 시 제약 조건 위반이 발생해야 한다.")
  void givenInvalidSlug_whenCreateDto_thenHaveViolation() {
    // null check
    CategoryCreateDto nullSlug = new CategoryCreateDto("test", null);
    Set<ConstraintViolation<CategoryCreateDto>> nullViolations = validator.validate(nullSlug);
    assertThat(nullViolations).isNotEmpty().hasSize(1);

    // Invalid Pattern
    CategoryCreateDto capital = new CategoryCreateDto("test", "CAPITAL-SLUG");
    CategoryCreateDto spaceSlug = new CategoryCreateDto("test", "space slug");
    List<CategoryCreateDto> invalidCases = List.of(capital, spaceSlug);
    for (CategoryCreateDto invalidDto : invalidCases) {
      Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(invalidDto);
      assertThat(violations).isNotEmpty().hasSize(1);
      assertThat(violations).map(ConstraintViolation::getMessage).allMatch(
          message -> message.contains(
              "Slug must be lowercase and can only contain letters, numbers and hyphens."));
    }
    CategoryCreateDto emptySlug = new CategoryCreateDto("test", "");
    Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(emptySlug);
    assertThat(violations).isNotEmpty().hasSize(2);
  }
}