package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.support.domain.entity.CategoryFixtureFactory;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  private Category category;

  @BeforeEach
  void setUp() {
    category = CategoryFixtureFactory.createFixture();
    ReflectionTestUtils.setField(category, "id", 1L);
  }

  @Test
  @DisplayName("카테고리 생성 테스트: 주어진 카테고리가 정상적으로 저장되어야 한다")
  void testCreateCategory() {
    // given
    given(categoryRepository.save(category)).willReturn(category);

    // when
    Category result = categoryService.createCategory(category);

    // then
    assertThat(result).isNotNull();
    then(categoryRepository).should().save(category);
  }

  @Test
  @DisplayName("카테고리 수정 테스트: 존재하는 카테고리의 정보를 수정할 수 있어야 한다")
  void testUpdateCategory() {
    // given
    given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
    Category updateParam = CategoryFixtureFactory.createFixture("updated-name", "updated-slug");

    // when
    Category result = categoryService.updateCategory(category.getId(), updateParam);

    // then
    assertThat(result.getName()).isEqualTo("updated-name");
    assertThat(result.getSlug()).isEqualTo("updated-slug");
  }

  @Test
  @DisplayName("카테고리 수정 실패 테스트: 존재하지 않는 카테고리 수정 시 예외가 발생한다")
  void testUpdateNonExistingCategory() {
    // given
    Long nonExistingId = 100L;
    Category updateParam = CategoryFixtureFactory.createFixture("updated", "updated");
    given(categoryRepository.findById(nonExistingId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.updateCategory(nonExistingId, updateParam))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("카테고리 삭제 테스트: 존재하는 카테고리를 ID로 삭제할 수 있어야 한다")
  void testDeleteCategory() {
    // given
    given(categoryRepository.existsById(category.getId())).willReturn(true);

    // when
    categoryService.deleteCategory(category.getId());

    // then
    then(categoryRepository).should().deleteById(category.getId());
  }

  @Test
  @DisplayName("카테고리 삭제 실패 테스트: 존재하지 않는 카테고리 삭제 시 예외가 발생한다")
  void testDeleteNonExistingCategory() {
    // given
    Long nonExistingId = 100L;
    given(categoryRepository.existsById(nonExistingId)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> categoryService.deleteCategory(nonExistingId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("ID로 카테고리 조회 테스트: 존재하는 ID로 조회 시 해당 카테고리를 반환한다")
  void testGetCategoryById() {
    // given
    given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));

    // when
    Category result = categoryService.getCategoryById(category.getId());

    // then
    assertThat(result).isEqualTo(category);
  }

  @Test
  @DisplayName("ID로 카테고리 조회 실패 테스트: 존재하지 않는 ID로 조회 시 예외가 발생한다")
  void testGetNonExistingCategory() {
    // given
    Long nonExistingId = 100L;
    given(categoryRepository.findById(nonExistingId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.getCategoryById(nonExistingId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
