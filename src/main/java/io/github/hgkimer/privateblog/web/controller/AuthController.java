package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import io.github.hgkimer.privateblog.web.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody LoginRequestDto request,
      HttpServletResponse response) {
    Authentication authReq = UsernamePasswordAuthenticationToken.unauthenticated(request.username(),
        request.password());
    Authentication authenticated = authenticationManager.authenticate(authReq);

    String token = jwtTokenProvider.generateToken((UserPrincipal) authenticated.getPrincipal());

    ResponseCookie cookie = ResponseCookie.from(jwtProperties.cookieName(), token)
        .httpOnly(true)
        .secure(jwtProperties.useSecureCookie())
        .path("/")
        .maxAge(Duration.ofHours(24))
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(jwtProperties.cookieName(), "")
        // 쿠키를 지울 때도 동일한 속성으로 맞추어야 브라우저가 정확하게 쿠키를 찾음
        .httpOnly(true)
        .secure(jwtProperties.useSecureCookie())
        .path("/")
        .maxAge(Duration.ZERO)
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok().build();
  }
}
