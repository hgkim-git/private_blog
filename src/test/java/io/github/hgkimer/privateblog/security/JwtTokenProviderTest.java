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
    jwtProperties = new JwtProperties(secretKey, 3600000L, "XSRF-TOKEN", false);
    jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    userPrincipal = UserPrincipal.builder()
        .username("test@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();
  }

  @Test
  @DisplayName("토큰이 정상적으로 생성되고 subject(username)가 추출되어야 한다.")
  void generateTokenAndExtractUsername() {
    String token = jwtTokenProvider.generateToken(userPrincipal);
    String username = jwtTokenProvider.extractUsername(token);

    assertThat(token).isNotBlank();
    assertThat(username).isEqualTo(userPrincipal.getUsername());
  }

  @Test
  @DisplayName("생성된 토큰을 검증하면 true를 반환해야 한다.")
  void validateToken_validToken() {
    String token = jwtTokenProvider.generateToken(userPrincipal);
    boolean isValid = jwtTokenProvider.validateToken(token, userPrincipal);

    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("다른 사용자의 정보로 토큰을 검증하면 false를 반환해야 한다.")
  void validateToken_invalidToken_differentUser() {
    String token = jwtTokenProvider.generateToken(userPrincipal);
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
    // 1ms 후 만료되는 토큰 속성
    JwtProperties shortLivedProps = new JwtProperties(jwtProperties.secretKey(), 1L, "XSRF-TOKEN",
        false);
    JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProps);

    String token = shortLivedProvider.generateToken(userPrincipal);

    // 만료 대기
    Thread.sleep(10);

    assertThrows(ExpiredJwtException.class, () -> shortLivedProvider.extractClaims(token));
  }
}
