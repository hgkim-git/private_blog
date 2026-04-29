package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PostSummaryResponseDto(
    @Schema(description = "포스트 ID", example = "1")
    Long id,
    CategoryResponseDto category,
    @Schema(description = "포스트 제목", example = "Spring Boot로 REST API 만들기")
    String title,
    @Schema(description = "포스트 요약", example = "Spring Boot로 REST API를 빠르게 구축하는 방법을 알아봅니다.")
    String summary,
    @Schema(description = "포스트 슬러그", example = "spring-boot-rest-api")
    String slug,
    @Schema(description = "포스트 상태", example = "PUBLISHED")
    String status,
    @Schema(description = "조회수", example = "42")
    Integer viewCount,
    @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,
    List<TagResponseDto> tags
) {

  public static PostSummaryResponseDto from(Post post) {
    Category category = post.getCategory();
    return new PostSummaryResponseDto(
        post.getId(),
        Optional.ofNullable(category).map(CategoryResponseDto::from).orElse(null),
        post.getTitle(),
        post.getSummary(),
        post.getSlug(),
        post.getStatus().name(),
        post.getViewCount(),
        post.getCreatedAt(),
        post.getPostTags()
            .stream()
            .map(postTag -> TagResponseDto.from(postTag.getTag()))
            .toList()
    );
  }
}
