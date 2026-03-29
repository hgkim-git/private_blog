package io.github.hgkimer.privateblog.support.web.dto;

import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
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
