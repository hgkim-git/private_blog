package io.github.hgkimer.privateblog.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secretKey,
    long accessTokenExpiration,
    long refreshTokenExpiration,
    String cookieName,
    String refreshCookieName,
    boolean useSecureCookie
) {

}