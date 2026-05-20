package io.github.hgkimer.blog.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiSummarizeRequestDto(
    @NotBlank
    @Size(max = 50000, message = "본문은 50,000자를 초과할 수 없습니다.")
    String content
) {

}
