package io.github.hgkimer.privateblog.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.support.domain.entity.CategoryFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {


  @Test
  @DisplayName("카테고리 빌더 테스트")
  void testBuilder() {
    Category category = Category.builder().name("test").slug("test").build();
    assertThat(category)
        .isNotNull()
        .hasFieldOrPropertyWithValue("name", "test")
        .hasFieldOrPropertyWithValue("slug", "test");
  }

  @Test
  @DisplayName("카테고리 업데이트 테스트")
  void testUpdate() {
    Category category = CategoryFixtureFactory.createFixture();
    category.update("test2", "test2");
    assertThat(category)
        .isNotNull()
        .hasFieldOrPropertyWithValue("name", "test2")
        .hasFieldOrPropertyWithValue("slug", "test2");
  }

}