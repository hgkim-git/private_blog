package io.github.hgkimer.blog.service.auth;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenStore {

  private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
  private static final String BLACKLIST_ACCESS_PREFIX = "blacklist:access:";

  private final StringRedisTemplate redisTemplate;

  public void saveRefreshToken(String username, String refreshToken, long expiration) {
    redisTemplate.opsForValue()
        .set(REFRESH_TOKEN_PREFIX + username, refreshToken, Duration.ofMillis(expiration));
  }

  public String getRefreshToken(String username) {
    try {
      return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
    } catch (Exception e) {
      log.warn("Redis getRefreshToken failed for user={}: {}", username, e.getMessage());
      return null;
    }
  }

  public void deleteRefreshToken(String username) {
    try {
      redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    } catch (Exception e) {
      log.error("Redis deleteRefreshToken failed for user={}: {}", username, e.getMessage());
    }
  }

  public void addTokenToBlacklist(String jti, long remainingTime) {
    if (remainingTime > 0) {
      try {
        redisTemplate.opsForValue().set(
            BLACKLIST_ACCESS_PREFIX + jti,
            "1",
            Duration.ofMillis(remainingTime)
        );
      } catch (Exception e) {
        log.error("Redis addTokenToBlacklist failed for jti={}: {}", jti, e.getMessage());
      }
    }
  }

  public boolean isTokenBlacklisted(String jti) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_ACCESS_PREFIX + jti));
    } catch (Exception e) {
      // Redis 장애 시 블랙리스트 체크를 건너뜀 (fail-open). 로그아웃한 토큰이 일시적으로 유효할 수 있음.
      log.warn("Redis isTokenBlacklisted failed for jti={}, failing open: {}", jti, e.getMessage());
      return false;
    }
  }

}
