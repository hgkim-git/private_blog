package io.github.hgkimer.privateblog.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record TagCreateDto(
    @Schema(description = "태그 이름", example = "Java")
    @NotBlank(message = "Tag name cannot be empty.")
    String name,
    @Schema(description = "태그 슬러그 (소문자, 숫자, 하이픈만 허용)", example = "java")
    @NotNull(message = "Slug cannot be null.")
    @Length(min = 1, max = 50, message = "Slug must be between 1 and 50 characters long.")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase and can only contain letters, numbers and hyphens.")
    String slug
) {

}
