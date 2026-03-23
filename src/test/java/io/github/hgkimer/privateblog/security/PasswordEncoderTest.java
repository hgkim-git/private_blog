package io.github.hgkimer.privateblog.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PasswordEncoderTest {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("비밀번호 암호화 테스트")
  void encodeTest() {
    String encodedPassword = passwordEncoder.encode("1234");
    System.out.println(encodedPassword);
    assertTrue(passwordEncoder.matches("1234", encodedPassword));
  }

}