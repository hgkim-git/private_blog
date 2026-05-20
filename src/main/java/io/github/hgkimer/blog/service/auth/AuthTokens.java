package io.github.hgkimer.blog.service.auth;

public record AuthTokens(
    String accessToken,
    String refreshToken,
    long accessTokenExpiration,
    long refreshTokenExpiration
) {

}
