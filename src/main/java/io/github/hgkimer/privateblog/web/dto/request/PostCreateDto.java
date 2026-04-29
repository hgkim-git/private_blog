package io.github.hgkimer.privateblog.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record PostCreateDto(
    @Schema(description = "카테고리 ID (미지정 시 null)", example = "1")
    Long categoryId,
    @Schema(description = "포스트 제목", example = "Spring Boot로 REST API 만들기")
    @NotBlank(message = "Title cannot be empty.")
    String title,
    @Schema(description = "포스트 본문 (Markdown)", example = "## 소개\n\nSpring Boot를 사용하면...")
    @NotNull(message = "Content cannot be null.")
    String content,
    @Schema(description = "포스트 요약", example = "Spring Boot로 REST API를 빠르게 구축하는 방법을 알아봅니다.")
    String summary,
    @Schema(description = "포스트 슬러그 (소문자, 숫자, 하이픈만 허용)", example = "spring-boot-rest-api")
    @NotBlank(message = "Slug cannot be empty.")
    @Length(min = 1, max = 250, message = "Slug must be between 1 and 250 characters long.")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase and can only contain letters, numbers and hyphens.")
    String slug,
    @Schema(description = "포스트 상태 (DRAFT 또는 PUBLISHED)", example = "PUBLISHED")
    @NotBlank(message = "Status cannot be empty.")
    @Pattern(regexp = "^(?i)(DRAFT|PUBLISHED)$", message = "Status must be either DRAFT or PUBLISHED.")
    String status,
    @Schema(description = "태그 ID 목록", example = "[1, 2, 3]")
    List<Long> tagIds
) {

}
