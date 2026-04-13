package io.github.hgkimer.privateblog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.service.auth.AuthTokenService;
import io.github.hgkimer.privateblog.service.auth.AuthTokens;
import io.github.hgkimer.privateblog.service.auth.RedisTokenStore;
import io.github.hgkimer.privateblog.service.auth.RotatedAuthTokens;
import io.github.hgkimer.privateblog.web.support.JwtTokenCookieManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
  private RedisTokenStore redisTokenStore;

  @Mock
  private AuthTokenService authTokenService;

  @Mock
  private JwtTokenCookieManager jwtTokenCookieManager;

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
    given(jwtProperties.cookieName()).willReturn("access-token");
  }

  @Test
  @DisplayName("access-token 쿠키가 없으면 인증 없이 다음 필터로 진행해야 한다.")
  void doFilterInternal_withoutCookie_skipsAuthentication() throws Exception {
    // given - no cookie set

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("HTML 요청에서 access-token 쿠키가 없고 refresh token이 유효하면 인증 정보가 등록되어야 한다.")
  void doFilterInternal_htmlRequestWithRefreshToken_setsAuthentication() throws Exception {
    // given
    request.setMethod("GET");
    request.setRequestURI("/posts/test-post");
    request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
    UserPrincipal userPrincipal = UserPrincipal.builder()
        .username("test@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();
    AuthTokens tokens = new AuthTokens("new-access-token", "new-refresh-token", 1800000L,
        86400000L);
    request.setCookies(new Cookie("refresh-token", "old-refresh-token"));
    given(jwtProperties.refreshCookieName()).willReturn("refresh-token");
    given(authTokenService.rotateRefreshToken("old-refresh-token")).willReturn(
        Optional.of(new RotatedAuthTokens(userPrincipal, tokens)));

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(authTokenService).should().rotateRefreshToken("old-refresh-token");
    then(jwtTokenCookieManager).should().addTokenCookies(tokens, response);
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
        "test@example.com");
  }

  @Test
  @DisplayName("API 요청에서는 refresh token fallback을 시도하지 않아야 한다.")
  void doFilterInternal_apiRequest_skipsRefreshFallback() throws Exception {
    // given
    request.setMethod("GET");
    request.setRequestURI("/api/posts");
    request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(authTokenService).should(never()).rotateRefreshToken("old-refresh-token");
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("정적 리소스 요청에서는 refresh token fallback을 시도하지 않아야 한다.")
  void doFilterInternal_staticResourceRequest_skipsRefreshFallback() throws Exception {
    // given
    request.setMethod("GET");
    request.setRequestURI("/js/common.js");
    request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(authTokenService).should(never()).rotateRefreshToken("old-refresh-token");
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("유효한 토큰 쿠키가 있으면 SecurityContext에 인증 정보가 등록되어야 한다.")
  void doFilterInternal_withValidToken_setsAuthentication() throws Exception {
    // given
    String token = "valid-token";
    String jti = "test-jti";
    String username = "test@example.com";
    request.setCookies(new Cookie("access-token", token));

    Claims claims = mock(Claims.class);
    given(claims.getSubject()).willReturn(username);
    given(claims.getId()).willReturn(jti);
    given(jwtTokenProvider.extractClaims(token)).willReturn(claims);
    given(redisTokenStore.isTokenBlacklisted(jti)).willReturn(false);

    UserPrincipal userPrincipal = UserPrincipal.builder()
        .username(username)
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();
    given(customUserDetailsService.loadUserByUsername(username)).willReturn(userPrincipal);
    given(jwtTokenProvider.validateToken(token, userPrincipal)).willReturn(true);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
        username);
  }

  @Test
  @DisplayName("블랙리스트에 등록된 jti를 가진 토큰은 인증이 거부되어야 한다.")
  void doFilterInternal_withBlacklistedToken_rejectsAuthentication() throws Exception {
    // given
    String token = "blacklisted-token";
    String jti = "blacklisted-jti";
    String username = "test@example.com";
    request.setCookies(new Cookie("access-token", token));

    Claims claims = mock(Claims.class);
    given(claims.getSubject()).willReturn(username);
    given(claims.getId()).willReturn(jti);
    given(jwtTokenProvider.extractClaims(token)).willReturn(claims);
    given(redisTokenStore.isTokenBlacklisted(jti)).willReturn(true);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("서명이 잘못되거나 만료된 토큰은 SecurityContext에 인증 정보가 등록되지 않아야 한다.")
  void doFilterInternal_withInvalidToken_skipsAuthentication() throws Exception {
    // given
    String token = "invalid-token";
    request.setCookies(new Cookie("access-token", token));

    given(jwtTokenProvider.extractClaims(token)).willThrow(new JwtException("Invalid token"));

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("토큰 파싱은 성공했지만 validateToken이 false를 반환하면 인증이 등록되지 않아야 한다.")
  void doFilterInternal_withNonValidatedToken_skipsAuthentication() throws Exception {
    // given
    String token = "parseable-but-invalid-token";
    String jti = "some-jti";
    String username = "test@example.com";
    request.setCookies(new Cookie("access-token", token));

    Claims claims = mock(Claims.class);
    given(claims.getSubject()).willReturn(username);
    given(claims.getId()).willReturn(jti);
    given(jwtTokenProvider.extractClaims(token)).willReturn(claims);
    given(redisTokenStore.isTokenBlacklisted(jti)).willReturn(false);

    UserPrincipal userPrincipal = UserPrincipal.builder()
        .username(username).password("password").build();
    given(customUserDetailsService.loadUserByUsername(username)).willReturn(userPrincipal);
    given(jwtTokenProvider.validateToken(token, userPrincipal)).willReturn(false);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
