package io.github.hgkimer.blog.support.web.dto;

import io.github.hgkimer.blog.domain.enums.PostStatus;
import io.github.hgkimer.blog.web.dto.request.PostCreateDto;
import java.util.List;

public class PostCreateDtoFixtureFactory {

  public static PostCreateDto createPostCreateDto() {
    return new PostCreateDto(
        null,
        "Test Title",
        "Test Content",
        "Test Summary",
        "test-slug",
        PostStatus.PUBLISHED.name(),
        List.of()
    );
  }

  public static PostCreateDto createPostCreateDtoWithInvalidStatus() {
    return new PostCreateDto(
        null,
        "Test Title",
        "Test Content",
        "Test Summary",
        "test-slug",
        "INVALID",
        List.of()
    );
  }

}
