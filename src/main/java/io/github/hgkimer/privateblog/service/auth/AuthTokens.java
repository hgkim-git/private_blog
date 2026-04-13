package io.github.hgkimer.privateblog.service.auth;

public record AuthTokens(
    String accessToken,
    String refreshToken,
    long accessTokenExpiration,
    long refreshTokenExpiration
) {

}
