package io.github.hgkimer.privateblog.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.support.domain.entity.CategoryFixtureFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CategoryRepositoryTest {

  @Autowired
  EntityManager entityManager;
  @Autowired
  private CategoryRepository categoryRepository;
  private Category category;


  @BeforeEach
  void setUp() {
    category = CategoryFixtureFactory.createFixture();
  }

  @Test
  @DisplayName("카테고리 생성 테스트")
  void testCreate() {
    Category savedCategory = categoryRepository.save(category);
    assertThat(savedCategory).isNotNull()
        .hasFieldOrPropertyWithValue("name", category.getName())
        .hasFieldOrPropertyWithValue("slug", category.getSlug())
        .extracting("id").isNotNull();
  }

  @Test
  @DisplayName("카테고리 수정 테스트")
  void testUpdate() {
    Category savedCategory = categoryRepository.save(category);
    savedCategory.update("업데이트", "업데이트된 slug");
    entityManager.flush();

    assertThat(savedCategory)
        .hasFieldOrPropertyWithValue("name", "업데이트")
        .hasFieldOrPropertyWithValue("slug", "업데이트된 slug");
  }

  @Test
  @DisplayName("카테고리 삭제 테스트")
  void testDelete() {
    Category savedCategory = categoryRepository.save(category);
    categoryRepository.delete(savedCategory);
    entityManager.flush();

    assertThat(categoryRepository.findAll()).isEmpty();
  }

}