package io.github.hgkimer.privateblog.web.dto.response;

import io.github.hgkimer.privateblog.domain.entity.Category;

public record CategoryResponseDto(
    Long id,
    String name,
    String slug
) {

    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName(), category.getSlug());
    }
}
