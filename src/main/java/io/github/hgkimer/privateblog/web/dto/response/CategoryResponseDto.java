package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponseDto(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,
    @Schema(description = "카테고리 이름", example = "Spring Boot")
    String name,
    @Schema(description = "카테고리 슬러그", example = "spring-boot")
    String slug,
    @Schema(description = "해당 카테고리의 포스트 수", example = "5")
    Integer postCount
) {

  public static CategoryResponseDto from(Category category) {
    return new CategoryResponseDto(category.getId(), category.getName(), category.getSlug(),
        category.getPostCount());
  }
}
