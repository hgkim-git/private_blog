package io.github.hgkimer.privateblog.support.domain.entity;

import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.UserRole;

public class UserFixtureFactory {

  public static User createFixture() {
    return User.builder().email("test@example.com").password("password").build();
  }

  public static User createFixture(String email, String password) {
    return User.builder().email(email).password(password).build();
  }

  public static User createAdminFixture() {
    return User.builder().email("test@example.com").password("password").role(UserRole.ADMIN)
        .build();
  }

  public static User createAdminFixture(String email) {
    return User.builder().email(email).password("password").role(UserRole.ADMIN)
        .build();
  }

}
