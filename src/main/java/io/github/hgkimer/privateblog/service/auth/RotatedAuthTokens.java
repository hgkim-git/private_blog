package io.github.hgkimer.privateblog.service.auth;

import io.github.hgkimer.privateblog.security.UserPrincipal;

public record RotatedAuthTokens(
    UserPrincipal userPrincipal,
    AuthTokens tokens
) {

}
