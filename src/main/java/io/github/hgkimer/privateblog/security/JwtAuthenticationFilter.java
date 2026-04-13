package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.util.JwtTokenResolver;
import io.github.hgkimer.privateblog.service.auth.AuthTokenService;
import io.github.hgkimer.privateblog.service.auth.RedisTokenStore;
import io.github.hgkimer.privateblog.service.auth.RotatedAuthTokens;
import io.github.hgkimer.privateblog.web.support.JwtTokenCookieManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtProperties jwtProperties;
  private final RedisTokenStore redisTokenStore;
  private final AuthTokenService authTokenService;
  private final JwtTokenCookieManager jwtTokenCookieManager;

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws ServletException, IOException {

    String token = JwtTokenResolver.resolveToken(request, jwtProperties.cookieName());
    if (token == null) {
      authenticateWithRefreshTokenIfAllowed(request, response);
      filterChain.doFilter(request, response);
      return;
    }
    try {
      Claims claims = jwtTokenProvider.extractClaims(token);
      String username = claims.getSubject();
      if (username == null) {
        throw new IllegalArgumentException("Invalid token");
      }
      // Access token blacklist check.
      String jti = claims.getId();
      if (redisTokenStore.isTokenBlacklisted(jti)) {
        log.warn("Blacklisted token accessed: {} / {}", jti, request.getRemoteAddr());
        throw new IllegalArgumentException("Token has been blacklisted");
      }
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      if (jwtTokenProvider.validateToken(token, userDetails)) {
        setAuthentication(userDetails, request);
      }
    } catch (ExpiredJwtException e) {
      // access token 만료 fallback
      authenticateWithRefreshTokenIfAllowed(request, response);
    } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
      SecurityContextHolder.clearContext();
    }
    filterChain.doFilter(request, response);
  }

  private void authenticateWithRefreshTokenIfAllowed(HttpServletRequest request,
      HttpServletResponse response) {
    if (!shouldAttemptRefreshFallback(request)) {
      return;
    }
    String refreshToken = JwtTokenResolver.resolveToken(request, jwtProperties.refreshCookieName());
    Optional<RotatedAuthTokens> rotated = authTokenService.rotateRefreshToken(refreshToken);
    rotated.ifPresent(result -> {
      jwtTokenCookieManager.addTokenCookies(result.tokens(), response);
      setAuthentication(result.userPrincipal(), request);
    });
  }

  private boolean shouldAttemptRefreshFallback(HttpServletRequest request) {
    if (!"GET".equalsIgnoreCase(request.getMethod())) {
      return false;
    }
    String uri = request.getRequestURI();
    if (uri.startsWith("/api/") || uri.equals("/admin/login")) {
      return false;
    }
    String accept = request.getHeader(HttpHeaders.ACCEPT);
    return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
  }

  private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()
    );
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
