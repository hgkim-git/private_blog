package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PostSummaryResponseDto(
    Long id,
    CategoryResponseDto category,
    String title,
    String summary,
    String slug,
    Integer viewCount,
    LocalDateTime createdAt,
    List<TagResponseDto> tags
) {

    public static PostSummaryResponseDto map(Post post) {
        Category category = post.getCategory();
        return new PostSummaryResponseDto(
            post.getId(),
            Optional.ofNullable(category).map(CategoryResponseDto::from).orElse(null),
            post.getTitle(),
            post.getSummary(),
            post.getSlug(),
            post.getViewCount(),
            post.getCreatedAt(),
            post.getPostTags()
                .stream()
                .map(postTag -> TagResponseDto.from(postTag.getTag()))
                .toList()
        );
    }
}
