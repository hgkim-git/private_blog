package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import io.github.hgkimer.privateblog.security.util.JwtTokenResolver;
import io.github.hgkimer.privateblog.service.auth.AuthTokenService;
import io.github.hgkimer.privateblog.service.auth.AuthTokens;
import io.github.hgkimer.privateblog.service.auth.RedisTokenStore;
import io.github.hgkimer.privateblog.service.auth.RotatedAuthTokens;
import io.github.hgkimer.privateblog.web.dto.request.LoginRequestDto;
import io.github.hgkimer.privateblog.web.support.JwtTokenCookieManager;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;
  private final RedisTokenStore redisTokenStore;
  private final AuthTokenService authTokenService;
  private final JwtTokenCookieManager jwtTokenCookieManager;

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody LoginRequestDto request,
      HttpServletResponse response) {
    Authentication authReq = UsernamePasswordAuthenticationToken.unauthenticated(request.username(),
        request.password());
    Authentication authenticated = authenticationManager.authenticate(authReq);
    UserPrincipal principal = (UserPrincipal) authenticated.getPrincipal();

    AuthTokens tokens = authTokenService.issueTokens(principal);
    jwtTokenCookieManager.addTokenCookies(tokens, response);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    jwtTokenCookieManager.removeTokenCookies(response);

    String accessToken = JwtTokenResolver.resolveToken(request, jwtProperties.cookieName());
    if (accessToken != null && jwtTokenProvider.isTokenAlive(accessToken)) {
      Claims claims = jwtTokenProvider.extractClaims(accessToken);
      String jti = claims.getId();
      String username = claims.getSubject();
      long expiration = claims.getExpiration().getTime();
      long remainingMillis = expiration - System.currentTimeMillis();
      redisTokenStore.addTokenToBlacklist(jti, remainingMillis);
      redisTokenStore.deleteRefreshToken(username);
    }

    return ResponseEntity.ok().build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = JwtTokenResolver.resolveToken(request, jwtProperties.refreshCookieName());
    Optional<RotatedAuthTokens> rotated = authTokenService.rotateRefreshToken(refreshToken);
    if (rotated.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    jwtTokenCookieManager.addTokenCookies(rotated.get().tokens(), response);
    return ResponseEntity.ok().build();
  }

}
