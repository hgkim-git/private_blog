package io.github.hgkimer.privateblog.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class JwtTokenResolver {

  public static String resolveToken(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(cookieName))
        .findAny()
        .map(Cookie::getValue)
        .orElse(null);
  }
}
