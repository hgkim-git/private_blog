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

class TagCreateDtoTest {

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
  @DisplayName("유효한 태그 생성 DTO 검증 테스트")
  void givenValid_WhenValidating_ThenConstraintViolations() {
    TagCreateDto dto = new TagCreateDto("valid", "valid");
    Set<ConstraintViolation<TagCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("이름 또는 슬러그가 비어있는 태그 생성 DTO 검증 시 제약 조건 위반이 발생해야 한다.")
  void givenNullOrEmpty_WhenValidating_ThenConstraintViolations() {
    TagCreateDto nullName = new TagCreateDto(null, "slug");
    TagCreateDto emptyName = new TagCreateDto("", "slug");
    TagCreateDto nullSlug = new TagCreateDto("name", null);

    List<TagCreateDto> nullOrEmptyNameDto = List.of(nullName, emptyName);
    nullOrEmptyNameDto.forEach(dto -> {
      Set<ConstraintViolation<TagCreateDto>> violations = validator.validate(dto);
      assertThat(violations).isNotEmpty().hasSize(1);
      assertThat(violations).map(ConstraintViolation::getMessage)
          .contains("Tag name cannot be empty.");
    });

    List<TagCreateDto> nullSlugDto = List.of(nullSlug);
    nullSlugDto.forEach(dto -> {
      Set<ConstraintViolation<TagCreateDto>> violations = validator.validate(dto);
      assertThat(violations).isNotEmpty().hasSize(1);
      assertThat(violations).map(ConstraintViolation::getMessage)
          .contains("Slug cannot be null.");
    });
  }

  @Test
  @DisplayName("유효하지 않은 형식의 슬러그를 가진 태그 생성 DTO 검증 시 제약 조건 위반이 발생해야 한다.")
  void givenInvalidSlug_WhenValidating_ThenConstraintViolations() {
    TagCreateDto invalidSlug = new TagCreateDto("name", "CAPITAL-contain");

    Set<ConstraintViolation<TagCreateDto>> violations = validator.validate(invalidSlug);
    assertThat(violations).isNotEmpty().hasSize(1);
    assertThat(violations).map(ConstraintViolation::getMessage)
        .contains("Slug must be lowercase and can only contain letters, numbers and hyphens.");
  }
}