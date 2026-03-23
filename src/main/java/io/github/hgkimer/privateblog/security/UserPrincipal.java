package io.github.hgkimer.privateblog.security;

import io.github.hgkimer.privateblog.domain.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

  private final String username;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  @Builder
  public UserPrincipal(String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    this.username = username;
    this.password = password;
    this.authorities = authorities;
  }

  public static UserPrincipal from(User user) {
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
    return UserPrincipal.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .authorities(authorities)
        .build();
  }

}
