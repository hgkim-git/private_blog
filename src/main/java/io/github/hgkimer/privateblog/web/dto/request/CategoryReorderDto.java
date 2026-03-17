package io.github.hgkimer.privateblog.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CategoryReorderDto(
    @NotNull(message = "Category id cannot be null.")
    @Positive(message = "Category id must be positive.")
    Long id,
    @NotNull(message = "Display order cannot be null.")
    @PositiveOrZero(message = "Display order must be positive or zero.")
    Integer displayOrder
) {

}
