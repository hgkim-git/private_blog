package io.github.hgkimer.privateblog.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequestDto(
    @Schema(description = "로그인 아이디 (이메일)", example = "admin@example.com")
    String username,
    @Schema(description = "비밀번호", example = "password1234!")
    String password
) {

}
