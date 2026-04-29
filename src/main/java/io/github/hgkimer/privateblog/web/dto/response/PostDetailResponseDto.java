package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PostDetailResponseDto(
    @Schema(description = "포스트 ID", example = "1")
    Long id,
    @Schema(description = "작성자 이메일", example = "admin@example.com")
    String author,
    CategoryResponseDto category,
    @Schema(description = "포스트 제목", example = "Spring Boot로 REST API 만들기")
    String title,
    @Schema(description = "포스트 본문 (Markdown 원문)", example = "## 소개\n\nSpring Boot를 사용하면...")
    String content,
    @Schema(description = "포스트 본문 (렌더링된 HTML)")
    String contentHtml,
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

  public static PostDetailResponseDto from(Post post) {
    Optional<Category> category = Optional.ofNullable(post.getCategory());
    List<TagResponseDto> tags = post.getPostTags().stream()
        .map(postTag -> TagResponseDto.from(postTag.getTag()))
        .toList();
    return new PostDetailResponseDto(
        post.getId(),
        post.getAuthor().getEmail(),
        category.map(CategoryResponseDto::from).orElse(null),
        post.getTitle(),
        post.getContent(),
        post.getContentHtml(),
        post.getSummary(),
        post.getSlug(),
        post.getStatus().name(),
        post.getViewCount(),
        post.getCreatedAt(),
        tags
    );
  }

}
