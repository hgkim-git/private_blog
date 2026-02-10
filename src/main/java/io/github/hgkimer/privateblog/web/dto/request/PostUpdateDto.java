package io.github.hgkimer.privateblog.web.dto.request;

import java.util.List;

public record PostUpdateDto(
    Long categoryId,
    String title,
    String content,
    String summary,
    String slug,
    String status,
    List<Long> tagsIds
) {

}
