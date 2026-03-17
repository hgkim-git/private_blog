package io.github.hgkimer.privateblog.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.hgkimer.privateblog.domain.entity.Category;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
    category = Category.builder().name("test").slug("test").build();
  }

  @Test
  void testCreate() {
    Category savedCategory = categoryRepository.save(category);
    assertNotNull(savedCategory);
    assertNotNull(savedCategory.getId());
  }

  @Test
  void testUpdate() {
    Category savedCategory = categoryRepository.save(category);
    savedCategory.update("업데이트", "업데이트된 slug");
    entityManager.flush();
    assertEquals("업데이트", savedCategory.getName());
    assertEquals("업데이트된 slug", savedCategory.getSlug());
  }

  @Test
  void testDelete() {
    Category savedCategory = categoryRepository.save(category);
    categoryRepository.delete(savedCategory);
    entityManager.flush();
    assertEquals(0, categoryRepository.findAll().size());
  }

}