package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateAccessToken(UserPrincipal userPrincipal) {
    return generateToken(userPrincipal, jwtProperties.accessTokenExpiration());
  }

  public String generateRefreshToken(UserPrincipal userPrincipal) {
    return generateToken(userPrincipal, jwtProperties.refreshTokenExpiration());
  }

  private String generateToken(UserPrincipal userPrincipal, long expiration) {
    return Jwts.builder().header()
        .and()
        .id(UUID.randomUUID().toString()) // jti
        .subject(userPrincipal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  // jti 추출
  public String extractJti(String token) {
    return extractClaims(token).getId();
  }

  // 사용자 추출
  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  // Expiration 추출
  public Date extractExpiration(String token) {
    return extractClaims(token).getExpiration();
  }

  // 클레임 추출
  public Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isTokenAlive(String token) {
    try {
      Date now = new Date();
      return extractExpiration(token).after(now);
    } catch (ExpiredJwtException e) {
      return false;
    }
  }

  // 토큰 검증
  public boolean validateToken(String token, UserDetails userDetails) {
    return userDetails.getUsername().equals(extractUsername(token)) && isTokenAlive(token);
  }

}
