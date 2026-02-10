package io.github.hgkimer.privateblog.web.dto.request;

public record CategoryUpdateDto(
    String name,
    String slug,
    Integer displayOrder
) {

}
