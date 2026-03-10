package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PostDetailResponseDto(
    Long id,
    String author,
    CategoryResponseDto category,
    String title,
    String content,
    String summary,
    String slug,
    String status,
    Integer viewCount,
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
