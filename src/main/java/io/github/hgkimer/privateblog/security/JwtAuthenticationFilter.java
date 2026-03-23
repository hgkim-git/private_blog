package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtProperties jwtProperties;

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws ServletException, IOException {

    // 쿠키에서 JWT 추출
    String token = resolveToken(request);

    // 쿠키가 없거나 JWT 쿠키가 없으면 인증 없이 다음 필터로 넘김
    // (인가가 필요한 요청이면 이후 ExceptionTranslationFilter → AuthenticationEntryPoint가 처리)
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // JWT에서 username(subject) 파싱
      String username = jwtTokenProvider.extractUsername(token);
      if (username == null) {
        throw new IllegalArgumentException("Invalid token");
      }

      // username으로 DB에서 사용자 정보 조회
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

      // 토큰 유효성 검증 (서명, 만료 등)
      if (jwtTokenProvider.validateToken(token, userDetails)) {

        // 5. 인증 객체 생성
        // UsernamePasswordAuthenticationToken: Spring Security의 인증 완료 상태를 나타내는 토큰
        //   - principal  : UserDetails (사용자 정보)
        //   - credentials: null (JWT 방식에서는 비밀번호를 다시 넘길 필요 없음)
        //   - authorities: 권한 목록 (ADMIN 등)
        //   - 3-arg 생성자를 사용하면 authenticated=true로 설정됨
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        // 요청 부가정보(IP, 세션ID 등) 추가
        // WebAuthenticationDetailsSource: HttpServletRequest에서 IP, 세션ID 등 부가 정보를 꺼내주는 팩토리
        // setDetails: 감사 로그나 추가 보안 처리에 활용 가능 (필수는 아님)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // SecurityContext에 인증 객체 등록
        // 이후 컨트롤러에서 @AuthenticationPrincipal, SecurityContextHolder.getContext().getAuthentication() 등으로 접근 가능
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

    } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
      // 토큰이 위변조되었거나 만료/형식 오류인 경우 SecurityContext를 비워 미인증 상태로 처리
      // → 이후 ExceptionTranslationFilter가 감지하여 AuthenticationEntryPoint(/admin/login) 로 리다이렉트
      SecurityContextHolder.clearContext();
    }

    // 8. 다음 필터로 요청 전달 (예외 발생 여부와 관계없이 항상 실행)
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(jwtProperties.cookieName()))
        .findAny()
        .map(Cookie::getValue)
        .orElse(null);
  }
}
