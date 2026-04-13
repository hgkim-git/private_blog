package io.github.hgkimer.privateblog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;
  private JwtProperties jwtProperties;
  private UserPrincipal userPrincipal;

  @BeforeEach
  void setUp() {
    // HS256 알고리즘에 필요한 크기(최소 256비트)의 시크릿 키 생성
    String secretKey = Base64.getEncoder()
        .encodeToString("my-secure-test-secret-key-that-is-long-enough-for-hs256".getBytes());
    jwtProperties = new JwtProperties(
        secretKey,
        3600000L,
        3600000L,
        "access-token",
        "refresh-token",
        false);
    jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    userPrincipal = UserPrincipal.builder()
        .username("test@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();
  }

  @Test
  @DisplayName("토큰이 정상적으로 생성되고 subject(username)가 추출되어야 한다.")
  void generateAccessTokenAndExtractUsername() {
    String token = jwtTokenProvider.generateAccessToken(userPrincipal);
    String username = jwtTokenProvider.extractUsername(token);

    assertThat(token).isNotBlank();
    assertThat(username).isEqualTo(userPrincipal.getUsername());
  }

  @Test
  @DisplayName("생성된 토큰을 검증하면 true를 반환해야 한다.")
  void validateToken_validToken() {
    String token = jwtTokenProvider.generateAccessToken(userPrincipal);
    boolean isValid = jwtTokenProvider.validateToken(token, userPrincipal);

    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("다른 사용자의 정보로 토큰을 검증하면 false를 반환해야 한다.")
  void validateToken_invalidToken_differentUser() {
    String token = jwtTokenProvider.generateAccessToken(userPrincipal);
    UserPrincipal otherUser = UserPrincipal.builder()
        .username("other@example.com")
        .password("password")
        .build();

    boolean isValid = jwtTokenProvider.validateToken(token, otherUser);

    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰의 클레임을 추출하려고 하면 ExpiredJwtException이 발생해야 한다.")
  void extractClaims_expiredToken() throws InterruptedException {
    JwtProperties shortLivedProps = new JwtProperties(jwtProperties.secretKey(),
        1L, 1L, "access-token", "refresh-token", false);
    JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProps);

    String token = shortLivedProvider.generateAccessToken(userPrincipal);
    Thread.sleep(10);

    assertThrows(ExpiredJwtException.class, () -> shortLivedProvider.extractClaims(token));
  }

  @Test
  @DisplayName("refresh token은 access token과 독립된 jti를 가져야 한다.")
  void generateRefreshToken_hasIndependentJti() {
    String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

    String accessJti = jwtTokenProvider.extractJti(accessToken);
    String refreshJti = jwtTokenProvider.extractJti(refreshToken);

    assertThat(accessJti).isNotBlank();
    assertThat(refreshJti).isNotBlank();
    assertThat(accessJti).isNotEqualTo(refreshJti);
  }

  @Test
  @DisplayName("extractJti는 토큰에서 UUID 형식의 jti를 반환해야 한다.")
  void extractJti_returnsUuidFormat() {
    String token = jwtTokenProvider.generateAccessToken(userPrincipal);

    String jti = jwtTokenProvider.extractJti(token);

    assertThat(jti).isNotBlank();
    // UUID 형식 검증 (8-4-4-4-12)
    assertThat(jti).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
  }

  @Test
  @DisplayName("유효한 토큰은 isTokenAlive가 true를 반환해야 한다.")
  void isTokenAlive_validToken_returnsTrue() {
    String token = jwtTokenProvider.generateAccessToken(userPrincipal);

    assertThat(jwtTokenProvider.isTokenAlive(token)).isTrue();
  }

  @Test
  @DisplayName("만료된 토큰은 isTokenAlive가 false를 반환해야 한다.")
  void isTokenAlive_expiredToken_returnsFalse() throws InterruptedException {
    JwtProperties shortLivedProps = new JwtProperties(jwtProperties.secretKey(),
        1L, 1L, "access-token", "refresh-token", false);
    JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProps);

    String token = shortLivedProvider.generateAccessToken(userPrincipal);
    Thread.sleep(10);

    assertThat(shortLivedProvider.isTokenAlive(token)).isFalse();
  }

  @Test
  @DisplayName("매번 생성하는 토큰의 jti는 중복되지 않아야 한다.")
  void generateToken_jtiIsUniquePerCall() {
    String token1 = jwtTokenProvider.generateAccessToken(userPrincipal);
    String token2 = jwtTokenProvider.generateAccessToken(userPrincipal);

    assertThat(jwtTokenProvider.extractJti(token1))
        .isNotEqualTo(jwtTokenProvider.extractJti(token2));
  }
}
