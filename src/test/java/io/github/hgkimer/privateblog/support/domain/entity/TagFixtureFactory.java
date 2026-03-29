package io.github.hgkimer.privateblog.support.domain.entity;

import io.github.hgkimer.privateblog.domain.entity.Tag;

public class TagFixtureFactory {

  public static Tag createFixture() {
    return Tag.builder().name("test").slug("test").build();
  }

  public static Tag createFixture(String name, String slug) {
    return Tag.builder().name(name).slug(slug).build();
  }

}
