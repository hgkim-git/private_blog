package io.github.hgkimer.blog.service.auth;

import io.github.hgkimer.blog.security.UserPrincipal;

public record RotatedAuthTokens(
    UserPrincipal userPrincipal,
    AuthTokens tokens
) {

}
