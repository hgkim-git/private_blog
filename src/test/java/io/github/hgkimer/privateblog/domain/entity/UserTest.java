package io.github.hgkimer.privateblog.domain.entity;

import io.github.hgkimer.privateblog.domain.enums.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("유저 빌더 테스트")
  void testBuilder() {
    User user = User.builder().email("test@example.com").password("password")
        .role(UserRole.VISITOR).build();
    Assertions.assertThat(user).isNotNull()
        .extracting("email", "password", "role")
        .containsExactly("test@example.com", "password", UserRole.VISITOR);
  }
}