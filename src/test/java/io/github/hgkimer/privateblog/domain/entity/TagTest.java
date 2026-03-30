package io.github.hgkimer.privateblog.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.support.domain.entity.TagFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TagTest {

  @Test
  @DisplayName("태그 빌더 테스트")
  void testBuilder() {
    Tag tag = Tag.builder().name("test").slug("test").build();
    assertThat(tag).isNotNull()
        .hasFieldOrPropertyWithValue("name", "test")
        .hasFieldOrPropertyWithValue("slug", "test");
  }

  @Test
  @DisplayName("태그 업데이트 테스트")
  void testUpdate() {
    Tag tag = TagFixtureFactory.createFixture();
    tag.update("test2", "test2");
    assertThat(tag).isNotNull()
        .hasFieldOrPropertyWithValue("name", "test2")
        .hasFieldOrPropertyWithValue("slug", "test2");
  }

}