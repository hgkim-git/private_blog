package io.github.hgkimer.privateblog.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CategoryReorderDto(
    @Schema(description = "카테고리 ID", example = "1")
    @NotNull(message = "Category id cannot be null.")
    @Positive(message = "Category id must be positive.")
    Long id,
    @Schema(description = "표시 순서 (0부터 시작)", example = "0")
    @NotNull(message = "Display order cannot be null.")
    @PositiveOrZero(message = "Display order must be positive or zero.")
    Integer displayOrder
) {

}
