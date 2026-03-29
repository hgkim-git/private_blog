package io.github.hgkimer.privateblog.web.dto.request;


import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.support.web.dto.PostCreateDtoFixtureFactory;
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

class PostCreateDtoTest {

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
  @DisplayName("유효한 게시글 생성 DTO 검증 테스트")
  void givenValidDto_whenCreateDto_thenSuccess() {
    PostCreateDto dto = PostCreateDtoFixtureFactory.createPostCreateDto();
    Set<ConstraintViolation<PostCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("유효하지 않은 상태 값을 가진 게시글 생성 DTO 검증 시 제약 조건 위반이 발생해야 한다.")
  void givenInvalidStatus_whenValidate_thenHaveViolation() {
    PostCreateDto dto = PostCreateDtoFixtureFactory.createPostCreateDtoWithInvalidStatus();
    Set<ConstraintViolation<PostCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isNotEmpty().hasSize(1);
    assertThat(violations).map(ConstraintViolation::getMessage)
        .contains("Status must be either DRAFT or PUBLISHED.");
  }

}