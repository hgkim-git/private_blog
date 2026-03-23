package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.persistence.jpa.UserRepository;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
    return UserPrincipal.from(user);
  }
}
