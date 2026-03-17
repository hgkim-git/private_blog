package io.github.hgkimer.privateblog.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CategoryTest {

  static Category mock() {
    return Category.builder().name("test").slug("test").build();
  }

  @Test
  void builder() {
    Category category = mock();
    assertNotNull(category);
    assertEquals("test", category.getName());
    assertEquals("test", category.getSlug());
  }

  @Test
  void update() {
    Category category = mock();
    category.update("test2", "test2");
    assertEquals("test2", category.getName());
    assertEquals("test2", category.getSlug());
    assertEquals(2, category.getDisplayOrder());
  }

}