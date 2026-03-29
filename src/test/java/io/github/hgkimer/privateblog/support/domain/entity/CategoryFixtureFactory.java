package io.github.hgkimer.privateblog.support.domain.entity;

import io.github.hgkimer.privateblog.domain.entity.Category;

public class CategoryFixtureFactory {

  public static Category createFixture() {
    return Category.builder().name("test").slug("test").build();
  }

  public static Category createFixture(String name, String slug) {
    return Category.builder().name(name).slug(slug).build();
  }

  public static Category createFixture(String name, String slug, Integer displayOrder) {
    return Category.builder().name(name).slug(slug).displayOrder(displayOrder).build();
  }


}

