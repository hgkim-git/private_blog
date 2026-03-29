package io.github.hgkimer.privateblog.support.domain.entity;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;

public class PostFixtureFactory {

  public static Post createFixture() {
    return Post.builder()
        .category(CategoryFixtureFactory.createFixture())
        .author(UserFixtureFactory.createAdminFixture())
        .title("test")
        .content("test")
        .contentHtml("<p>test</p>")
        .summary("test")
        .slug("test")
        .status(PostStatus.PUBLISHED)
        .build();
  }

  public static Post createFixture(User author) {
    return Post.builder()
        .category(null)
        .author(author)
        .title("test")
        .content("test")
        .contentHtml("<p>test</p>")
        .summary("test")
        .slug("test")
        .status(PostStatus.PUBLISHED)
        .build();
  }

  public static Post createFixture(Category category, User author) {
    return Post.builder()
        .category(category)
        .author(author)
        .title("test")
        .content("test")
        .contentHtml("<p>test</p>")
        .summary("test")
        .slug("test")
        .status(PostStatus.PUBLISHED)
        .build();
  }

  public static Post createFixture(Category category, User author, String title, String slug,
      PostStatus status) {
    return Post.builder()
        .category(category)
        .author(author)
        .title(title)
        .content("test")
        .contentHtml("<p>test</p>")
        .summary("test")
        .slug(slug)
        .status(status)
        .build();
  }

}

