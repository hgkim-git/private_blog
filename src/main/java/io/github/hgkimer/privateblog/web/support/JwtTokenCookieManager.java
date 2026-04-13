package io.github.hgkimer.privateblog.web.support;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.service.auth.AuthTokens;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenCookieManager {

  private static final String ROOT_PATH = "/";

  private final JwtProperties jwtProperties;

  public void addTokenCookies(AuthTokens tokens, HttpServletResponse response) {
    setCookie(jwtProperties.cookieName(), tokens.accessToken(), tokens.accessTokenExpiration(),
        response);
    setCookie(jwtProperties.refreshCookieName(), tokens.refreshToken(),
        tokens.refreshTokenExpiration(), response);
  }

  public void removeTokenCookies(HttpServletResponse response) {
    removeCookie(jwtProperties.cookieName(), response);
    removeCookie(jwtProperties.refreshCookieName(), response);
  }

  private void setCookie(@NotNull String cookieName, @NotNull String value,
      @NotNull long expiration, HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, value)
        .httpOnly(true)
        .secure(jwtProperties.useSecureCookie())
        .path(ROOT_PATH)
        .maxAge(Duration.ofMillis(expiration))
        .sameSite(SameSite.STRICT.name())
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void removeCookie(String cookieName, HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, "")
        .httpOnly(true)
        .secure(jwtProperties.useSecureCookie())
        .path(ROOT_PATH)
        .maxAge(Duration.ZERO)
        .sameSite(SameSite.STRICT.name())
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
