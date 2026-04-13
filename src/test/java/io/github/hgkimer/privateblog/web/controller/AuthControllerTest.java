package io.github.hgkimer.privateblog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hgkimer.privateblog.config.SecurityConfig;
import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.CustomUserDetailsService;
import io.github.hgkimer.privateblog.security.JwtAuthenticationFilter;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import io.github.hgkimer.privateblog.service.auth.AuthTokenService;
import io.github.hgkimer.privateblog.service.auth.RedisTokenStore;
import io.github.hgkimer.privateblog.web.dto.request.LoginRequestDto;
import io.github.hgkimer.privateblog.web.support.JwtTokenCookieManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthTokenService.class,
    JwtTokenCookieManager.class})
class AuthControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtProperties jwtProperties;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @MockitoBean
  private RedisTokenStore redisTokenStore;

  @BeforeEach
  void setUp() {
    given(jwtProperties.cookieName()).willReturn("access-token");
    given(jwtProperties.refreshCookieName()).willReturn("refresh-token");
    given(jwtProperties.useSecureCookie()).willReturn(false);
    given(jwtProperties.accessTokenExpiration()).willReturn(1800000L);
    given(jwtProperties.refreshTokenExpiration()).willReturn(86400000L);
  }

  // ──────────────────────────── /login ────────────────────────────

  @Test
  @DisplayName("올바른 인증 정보로 로그인 시 access token과 refresh token 쿠키가 모두 발급되어야 한다.")
  void login_success_issuesBothCookies() {
    // given
    LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "password");
    UserPrincipal principal = UserPrincipal.builder()
        .username("test@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();

    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    given(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).willReturn(
        "mocked-access-token");
    given(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).willReturn(
        "mocked-refresh-token");

    // when & then
    mvc.post().uri("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto))
        .assertThat()
        .hasStatusOk()
        .headers().hasEntrySatisfying(HttpHeaders.SET_COOKIE, values -> {
          assertThat(values).anyMatch(v -> v.contains("access-token=mocked-access-token")
              && v.contains("HttpOnly") && v.contains("SameSite=STRICT") && v.contains("Path=/"));
          assertThat(values).anyMatch(v -> v.contains("refresh-token=mocked-refresh-token")
              && v.contains("HttpOnly") && v.contains("Path=/"));
        });

    then(redisTokenStore).should()
        .saveRefreshToken(eq("test@example.com"), eq("mocked-refresh-token"), eq(86400000L));
  }

  @Test
  @DisplayName("잘못된 인증 정보로 로그인 시 401 응답이 반환되어야 한다.")
  void login_badCredentials_returns401() {
    // given
    LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "wrong-password");
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // when & then
    assertThat(mvc.post().uri("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .headers().doesNotContainHeader(HttpHeaders.SET_COOKIE);
  }

  // ──────────────────────────── /logout ────────────────────────────

  @Test
  @DisplayName("로그아웃 시 access token과 refresh token 쿠키 모두 Max-Age=0으로 만료 처리되어야 한다.")
  @WithMockUser(username = "test@example.com", authorities = "ADMIN")
  void logout_success_expiresBothCookies() {
    // when & then
    mvc.post().uri("/api/auth/logout")
        .with(csrf())
        .assertThat()
        .hasStatusOk()
        .headers().hasEntrySatisfying(HttpHeaders.SET_COOKIE,
            values -> assertThat(values).allMatch(v -> v.contains("Max-Age=0")));
  }

  @Test
  @DisplayName("유효한 access token 쿠키로 로그아웃 시 jti가 블랙리스트에 등록되고 refresh token이 삭제되어야 한다.")
  @WithMockUser(username = "test@example.com", authorities = "ADMIN")
  void logout_withValidAccessToken_blacklistsJtiAndDeletesRefreshToken() {
    // given
    String accessTokenValue = "valid-access-token";
    long futureTime = System.currentTimeMillis() + 1800000L;

    Claims claims = mock(Claims.class);
    given(claims.getId()).willReturn("test-jti");
    given(claims.getSubject()).willReturn("test@example.com");
    given(claims.getExpiration()).willReturn(new Date(futureTime));

    given(jwtTokenProvider.extractClaims(accessTokenValue)).willReturn(claims);
    given(jwtTokenProvider.isTokenAlive(accessTokenValue)).willReturn(true);

    // when
    mvc.post().uri("/api/auth/logout")
        .with(csrf())
        .cookie(new Cookie("access-token", accessTokenValue))
        .assertThat()
        .hasStatusOk();

    // then
    then(redisTokenStore).should().addTokenToBlacklist(eq("test-jti"), anyLong());
    then(redisTokenStore).should().deleteRefreshToken("test@example.com");
  }

  @Test
  @DisplayName("access token 쿠키 없이 로그아웃 시 블랙리스트 등록 없이 200 OK가 반환되어야 한다.")
  @WithMockUser(username = "test@example.com", authorities = "ADMIN")
  void logout_withoutAccessToken_skipsBlacklist() {
    // when
    mvc.post().uri("/api/auth/logout")
        .with(csrf())
        .assertThat()
        .hasStatusOk();

    // then
    then(redisTokenStore).should(never()).addTokenToBlacklist(anyString(), anyLong());
    then(redisTokenStore).should(never()).deleteRefreshToken(anyString());
  }

  @Test
  @DisplayName("인증되지 않은 상태에서 로그아웃 시 401 응답이 반환되어야 한다.")
  void logout_unauthenticated_returns401() {
    // when & then
    assertThat(mvc.post().uri("/api/auth/logout").with(csrf()))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  // ──────────────────────────── /refresh ────────────────────────────

  @Test
  @DisplayName("유효한 refresh token으로 요청 시 새 access token과 새 refresh token 쿠키가 발급되어야 한다 (Rotation).")
  void refresh_success_rotatesBothTokens() {
    // given
    String oldRefreshToken = "old-refresh-token";
    UserPrincipal userPrincipal = UserPrincipal.builder()
        .username("user@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();

    given(jwtTokenProvider.extractUsername(oldRefreshToken)).willReturn("user@example.com");
    given(redisTokenStore.getRefreshToken("user@example.com")).willReturn(oldRefreshToken);
    given(customUserDetailsService.loadUserByUsername("user@example.com")).willReturn(
        userPrincipal);
    given(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).willReturn(
        "new-access-token");
    given(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).willReturn(
        "new-refresh-token");

    // when & then
    mvc.post().uri("/api/auth/refresh")
        .with(csrf())
        .cookie(new Cookie("refresh-token", oldRefreshToken))
        .assertThat()
        .hasStatusOk()
        .headers().hasEntrySatisfying(HttpHeaders.SET_COOKIE, values -> {
          assertThat(values).anyMatch(v -> v.contains("access-token=new-access-token"));
          assertThat(values).anyMatch(v -> v.contains("refresh-token=new-refresh-token"));
        });

    then(redisTokenStore).should()
        .saveRefreshToken("user@example.com", "new-refresh-token", 86400000L);
  }

  @Test
  @DisplayName("refresh token 쿠키가 없으면 401 응답이 반환되어야 한다.")
  void refresh_noCookie_returns401() {
    // when & then
    assertThat(mvc.post().uri("/api/auth/refresh").with(csrf()))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("Redis에 저장된 refresh token과 불일치할 경우 탈취로 간주하여 저장된 토큰을 삭제하고 401을 반환해야 한다.")
  void refresh_mismatchedToken_deletesStoredTokenAndReturns401() {
    // given - 클라이언트가 구 refresh token을 보내지만, Redis에는 이미 새 토큰이 저장된 상태
    String oldRefreshToken = "old-refresh-token";
    String currentStoredToken = "new-refresh-token-in-redis";

    given(jwtTokenProvider.extractUsername(oldRefreshToken)).willReturn("user@example.com");
    given(redisTokenStore.getRefreshToken("user@example.com")).willReturn(currentStoredToken);

    // when
    assertThat(mvc.post().uri("/api/auth/refresh")
        .with(csrf())
        .cookie(new Cookie("refresh-token", oldRefreshToken)))
        .hasStatus(HttpStatus.UNAUTHORIZED);

    // then - 탈취 감지: 저장된 refresh token 삭제로 전체 세션 무효화
    then(redisTokenStore).should().deleteRefreshToken("user@example.com");
  }

  @Test
  @DisplayName("만료되거나 위변조된 refresh token으로 요청 시 401 응답이 반환되어야 한다.")
  void refresh_expiredOrInvalidToken_returns401() {
    // given
    given(jwtTokenProvider.extractUsername(anyString()))
        .willThrow(new JwtException("Token expired or invalid"));

    // when & then
    assertThat(mvc.post().uri("/api/auth/refresh")
        .with(csrf())
        .cookie(new Cookie("refresh-token", "expired-token")))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("Redis에 저장된 refresh token이 없으면 401 응답이 반환되어야 한다.")
  void refresh_tokenNotFoundInRedis_returns401() {
    // given - Redis에 해당 username의 refresh token이 없음 (TTL 만료 등)
    String refreshToken = "some-refresh-token";
    given(jwtTokenProvider.extractUsername(refreshToken)).willReturn("user@example.com");
    given(redisTokenStore.getRefreshToken("user@example.com")).willReturn(null);

    // when & then
    assertThat(mvc.post().uri("/api/auth/refresh")
        .with(csrf())
        .cookie(new Cookie("refresh-token", refreshToken)))
        .hasStatus(HttpStatus.UNAUTHORIZED);

    then(redisTokenStore).should().deleteRefreshToken("user@example.com");
  }

  private String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
