package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;

  public SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // 토큰 생성
  public String generateToken(UserPrincipal userPrincipal) {
    return Jwts.builder().header()
        .and()
        .subject(userPrincipal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()))
        .signWith(getSigningKey())
        .compact();
  }

  // 사용자 추출
  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  // 클레임 추출
  public Claims extractClaims(String token) {
    return Jwts.parser().
        verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  // 토큰 검증
  public boolean validateToken(String token, UserDetails userDetails) {
    return userDetails.getUsername().equals(extractUsername(token));
  }

}
