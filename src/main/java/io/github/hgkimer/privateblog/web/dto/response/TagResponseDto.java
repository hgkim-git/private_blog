package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

public record TagResponseDto(
    @Schema(description = "태그 ID", example = "1")
    Long id,
    @Schema(description = "태그 이름", example = "Java")
    String name,
    @Schema(description = "태그 슬러그", example = "java")
    String slug,
    @Schema(description = "해당 태그의 포스트 수", example = "10")
    Long postCount
) {

  public static TagResponseDto from(Tag tag) {
    return new TagResponseDto(tag.getId(), tag.getName(), tag.getSlug(), 0L);
  }
}
