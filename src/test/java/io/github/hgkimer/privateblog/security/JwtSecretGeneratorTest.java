package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.util.JwtSecretGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class JwtSecretGeneratorTest {

  @Test
  @DisplayName("JWT 비밀 키 생성 테스트")
  public void testGenerateSecretKey() {
    String encodedSecretKey = JwtSecretGenerator.generateSecret();
    Assertions.assertThat(encodedSecretKey).isNotEmpty();
  }
}