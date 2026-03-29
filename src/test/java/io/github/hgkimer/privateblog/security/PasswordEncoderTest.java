package io.github.hgkimer.privateblog.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PasswordEncoderTest {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  @DisplayName("비밀번호 암호화 테스트")
  void encodeTest() {
    String encodedPassword = passwordEncoder.encode("1234");
    assertThat(encodedPassword).isNotBlank()
        .satisfies(encPass -> passwordEncoder.matches("1234", encPass));
  }

}