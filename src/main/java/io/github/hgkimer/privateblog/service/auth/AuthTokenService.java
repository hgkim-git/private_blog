package io.github.hgkimer.privateblog.service.auth;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.CustomUserDetailsService;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;
  private final RedisTokenStore redisTokenStore;
  private final CustomUserDetailsService customUserDetailsService;

  public AuthTokens issueTokens(UserPrincipal principal) {
    String accessToken = jwtTokenProvider.generateAccessToken(principal);
    String refreshToken = jwtTokenProvider.generateRefreshToken(principal);
    long accessTokenExpiration = jwtProperties.accessTokenExpiration();
    long refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
    redisTokenStore.saveRefreshToken(principal.getUsername(), refreshToken, refreshTokenExpiration);
    return new AuthTokens(accessToken, refreshToken, accessTokenExpiration, refreshTokenExpiration);
  }

  public Optional<RotatedAuthTokens> rotateRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      return Optional.empty();
    }

    try {
      String username = jwtTokenProvider.extractUsername(refreshToken);
      String storedRefreshToken = redisTokenStore.getRefreshToken(username);
      if (!refreshToken.equals(storedRefreshToken)) {
        redisTokenStore.deleteRefreshToken(username);
        return Optional.empty();
      }

      UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(
          username);
      return Optional.of(new RotatedAuthTokens(userPrincipal, issueTokens(userPrincipal)));
    } catch (JwtException | UsernameNotFoundException | ClassCastException e) {
      return Optional.empty();
    }
  }
}
