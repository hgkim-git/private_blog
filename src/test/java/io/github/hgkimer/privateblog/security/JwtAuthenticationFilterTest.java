package io.github.hgkimer.privateblog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private CustomUserDetailsService customUserDetailsService;

  @Mock
  private JwtProperties jwtProperties;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  @DisplayName("토큰 쿠키가 존재하지 않으면 인증 없이 다음 필터로 진행한다.")
  void doFilterInternal_withoutToken() throws Exception {
    lenient().when(jwtProperties.cookieName()).thenReturn("access-token");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("유효한 토큰 쿠키가 존재하면 SecurityContext에 인증 정보가 등록된다.")
  void doFilterInternal_withValidToken() throws Exception {
    String token = "valid-token";
    String username = "test@example.com";
    Cookie cookie = new Cookie("access-token", token);
    request.setCookies(cookie);

    when(jwtProperties.cookieName()).thenReturn("access-token");
    when(jwtTokenProvider.extractUsername(token)).thenReturn(username);

    UserPrincipal userPrincipal = UserPrincipal.builder()
        .username(username)
        .password("password")
        .build();

    when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userPrincipal);
    when(jwtTokenProvider.validateToken(token, userPrincipal)).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
        username);
  }

  @Test
  @DisplayName("유효하지 않은 토큰(예외 발생)일 경우 인증 정보가 등록되지 않는다.")
  void doFilterInternal_withInvalidToken() throws Exception {
    String token = "invalid-token";
    Cookie cookie = new Cookie("access-token", token);
    request.setCookies(cookie);

    when(jwtProperties.cookieName()).thenReturn("access-token");
    when(jwtTokenProvider.extractUsername(token)).thenThrow(
        new io.jsonwebtoken.JwtException("Invalid token"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
