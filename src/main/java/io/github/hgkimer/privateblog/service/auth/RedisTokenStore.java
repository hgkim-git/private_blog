package io.github.hgkimer.privateblog.service.auth;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenStore {

  private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
  private static final String BLACKLIST_ACCESS_PREFIX = "blacklist:access:";

  private final StringRedisTemplate redisTemplate;

  public void saveRefreshToken(String username, String refreshToken, long expiration) {
    redisTemplate.opsForValue()
        .set(REFRESH_TOKEN_PREFIX + username, refreshToken, Duration.ofMillis(expiration));
  }

  public String getRefreshToken(String username) {
    return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
  }

  public void deleteRefreshToken(String username) {
    redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
  }

  public void addTokenToBlacklist(String jti, long remainingTime) {
    if (remainingTime > 0) {
      redisTemplate.opsForValue().set(
          BLACKLIST_ACCESS_PREFIX + jti,
          "1",
          Duration.ofMillis(remainingTime)
      );
    }
  }

  public boolean isTokenBlacklisted(String jti) {
    // Nullable
    return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_ACCESS_PREFIX + jti));
  }

}
