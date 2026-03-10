package io.github.hgkimer.privateblog.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record PostCreateDto(
    Long categoryId,
    @NotBlank(message = "Author cannot be empty.")
    @Email(message = "Author must be a valid email address.")
    String author,
    @NotBlank(message = "Title cannot be empty.")
    String title,
    @NotNull(message = "Content cannot be null.")
    String content,
    String summary,
    @NotBlank(message = "Slug cannot be empty.")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase and can only contain letters, numbers and hyphens.")
    String slug,
    @NotBlank(message = "Status cannot be empty.")
    @Pattern(regexp = "^(DRAFT|PUBLISHED)$", message = "Status must be either DRAFT or PUBLISHED.")
    String status,
    List<Long> tagIds
) {

}
