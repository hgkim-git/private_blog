package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Post;
import java.util.List;

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
    List<TagResponseDto> tags
) {

    public static PostDetailResponseDto from(Post post) {
        List<TagResponseDto> tags = post.getPostTags().stream()
            .map(postTag -> TagResponseDto.from(postTag.getTag()))
            .toList();
        return new PostDetailResponseDto(
            post.getId(),
            post.getAuthor().getEmail(),
            CategoryResponseDto.from(post.getCategory()),
            post.getTitle(),
            post.getContent(),
            post.getSummary(),
            post.getSlug(),
            post.getStatus().name(),
            post.getViewCount(),
            tags
        );
    }

}
