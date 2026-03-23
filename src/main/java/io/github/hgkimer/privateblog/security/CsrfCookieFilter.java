package io.github.hgkimer.privateblog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security 6에서 CookieCsrfTokenRepository는 CSRF 토큰을 lazy하게 로드한다. 즉, 토큰이 명시적으로 접근되지 않으면
 * XSRF-TOKEN 쿠키가 응답에 set되지 않는다. Thymeleaf form이 아닌 API 기반 앱에서는 JS가 쿠키를 읽을 수 없어 CSRF 검증이 실패한다. 이 필터는
 * 매 요청마다 csrfToken.getToken()을 호출하여 쿠키가 항상 응답에 포함되도록 강제한다.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws ServletException, IOException {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    // getToken() 호출이 쿠키 set을 트리거함
    if (csrfToken != null) {
      csrfToken.getToken();
    }
    filterChain.doFilter(request, response);
  }
}
