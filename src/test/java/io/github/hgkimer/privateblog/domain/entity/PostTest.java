package io.github.hgkimer.privateblog.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.support.domain.entity.PostFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PostTest {

  @Test
  @DisplayName("게시글 빌더 테스트")
  void testBuilder() {
    Post post = Post.builder().title("test").content("test").build();
    assertThat(post).isNotNull()
        .hasFieldOrPropertyWithValue("title", "test")
        .hasFieldOrPropertyWithValue("content", "test");

  }

  @Test
  @DisplayName("게시글 업데이트 테스트")
  void testUpdate() {
    Post post = PostFixtureFactory.createFixture();
    post.update(
        "changed title",
        "changed content",
        post.getContentHtml(),
        post.getSummary(),
        post.getSlug(),
        post.getCategory()
    );
    assertThat(post).isNotNull()
        .hasFieldOrPropertyWithValue("title", "changed title")
        .hasFieldOrPropertyWithValue("content", "changed content");

    post.publish();
    assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    post.draft();
    assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
  }

}
