package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.privateblog.service.auth.RedisTokenStore;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenStoreTest {

  private static final String REFRESH_PREFIX = "refresh:token:";
  private static final String BLACKLIST_PREFIX = "blacklist:access:";

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private RedisTokenStore redisTokenStore;

  @Test
  @DisplayName("refresh token 저장 시 지정한 TTL로 Redis에 저장되어야 한다.")
  void saveRefreshToken_storesWithCorrectKeyAndTtl() {
    // given
    String username = "user@example.com";
    String refreshToken = "refresh-token-value";
    long expiration = 86400000L;
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // when
    redisTokenStore.saveRefreshToken(username, refreshToken, expiration);

    // then
    then(valueOperations).should()
        .set(REFRESH_PREFIX + username, refreshToken, Duration.ofMillis(expiration));
  }

  @Test
  @DisplayName("저장된 refresh token 조회 시 Redis에서 반환된 값과 일치해야 한다.")
  void getRefreshToken_returnsStoredValue() {
    // given
    String username = "user@example.com";
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(REFRESH_PREFIX + username)).willReturn("stored-token");

    // when
    String result = redisTokenStore.getRefreshToken(username);

    // then
    assertThat(result).isEqualTo("stored-token");
  }

  @Test
  @DisplayName("Redis에 저장되지 않은 username 조회 시 null을 반환해야 한다.")
  void getRefreshToken_returnsNullWhenNotFound() {
    // given
    String username = "unknown@example.com";
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(REFRESH_PREFIX + username)).willReturn(null);

    // when
    String result = redisTokenStore.getRefreshToken(username);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("refresh token 삭제 시 해당 Redis 키가 제거되어야 한다.")
  void deleteRefreshToken_deletesCorrectKey() {
    // given
    String username = "user@example.com";

    // when
    redisTokenStore.deleteRefreshToken(username);

    // then
    then(redisTemplate).should().delete(REFRESH_PREFIX + username);
  }

  @Test
  @DisplayName("남은 유효 시간이 양수일 때 블랙리스트에 jti를 TTL과 함께 저장해야 한다.")
  void addTokenToBlacklist_storesWithTtl_whenRemainingTimePositive() {
    // given
    String jti = "test-jti-uuid";
    long remainingMillis = 60000L;
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // when
    redisTokenStore.addTokenToBlacklist(jti, remainingMillis);

    // then
    then(valueOperations).should()
        .set(BLACKLIST_PREFIX + jti, "1", Duration.ofMillis(remainingMillis));
  }

  @Test
  @DisplayName("남은 유효 시간이 0이면 블랙리스트에 저장하지 않아야 한다.")
  void addTokenToBlacklist_doesNotStore_whenRemainingTimeIsZero() {
    // given
    String jti = "test-jti-uuid";

    // when
    redisTokenStore.addTokenToBlacklist(jti, 0L);

    // then
    then(valueOperations).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("남은 유효 시간이 음수이면 블랙리스트에 저장하지 않아야 한다.")
  void addTokenToBlacklist_doesNotStore_whenRemainingTimeNegative() {
    // given
    String jti = "test-jti-uuid";

    // when
    redisTokenStore.addTokenToBlacklist(jti, -1000L);

    // then
    then(valueOperations).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("블랙리스트에 등록된 jti 조회 시 true를 반환해야 한다.")
  void isTokenBlacklisted_returnsTrue_whenKeyExists() {
    // given
    String jti = "blacklisted-jti";
    given(redisTemplate.hasKey(BLACKLIST_PREFIX + jti)).willReturn(true);

    // when
    boolean result = redisTokenStore.isTokenBlacklisted(jti);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("블랙리스트에 없는 jti 조회 시 false를 반환해야 한다.")
  void isTokenBlacklisted_returnsFalse_whenKeyNotExists() {
    // given
    String jti = "normal-jti";
    given(redisTemplate.hasKey(BLACKLIST_PREFIX + jti)).willReturn(false);

    // when
    boolean result = redisTokenStore.isTokenBlacklisted(jti);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Redis가 null을 반환하더라도 블랙리스트 결과는 false여야 한다.")
  void isTokenBlacklisted_returnsFalse_whenRedisReturnsNull() {
    // given
    String jti = "some-jti";
    given(redisTemplate.hasKey(BLACKLIST_PREFIX + jti)).willReturn(null);

    // when
    boolean result = redisTokenStore.isTokenBlacklisted(jti);

    // then
    assertThat(result).isFalse();
  }
}
