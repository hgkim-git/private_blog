package io.github.hgkimer.privateblog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hgkimer.privateblog.config.SecurityConfig;
import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.JwtAuthenticationFilter;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.security.UserPrincipal;
import io.github.hgkimer.privateblog.service.CustomUserDetailsService;
import io.github.hgkimer.privateblog.web.dto.request.LoginRequestDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtProperties jwtProperties;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @BeforeEach
  void setUp() {
    // JwtProperties setup
    when(jwtProperties.cookieName()).thenReturn("access-token");
    when(jwtProperties.useSecureCookie()).thenReturn(false);
  }

  @Test
  @DisplayName("올바른 인증 정보로 로그인 시 200 OK와 함께 JWT 쿠키가 발급되어야 한다.")
  void login_success() {
    // Given
    LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "password");
    UserPrincipal principal = UserPrincipal.builder()
        .username("test@example.com")
        .password("password")
        .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
        .build();

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    when(jwtTokenProvider.generateToken(principal)).thenReturn("mocked-jwt-token");

    // When & Then
    mvc.post().uri("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto))
        .assertThat()
        .hasStatusOk()
        .headers().containsHeader(HttpHeaders.SET_COOKIE)
        .hasEntrySatisfying(HttpHeaders.SET_COOKIE, values -> {
          values.forEach(v -> {
            assertThat(v).contains("access-token=mocked-jwt-token");
            assertThat(v).contains("HttpOnly");
            assertThat(v).contains("SameSite=Strict");
          });
        });
  }

  @Test
  @DisplayName("잘못된 인증 정보로 로그인 시 401 응답이 반환되어야 한다.")
  void login_bad_credentials() {
    // Given
    LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "wrong-password");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When & Then
    assertThat(mvc.post().uri("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .headers().doesNotContainHeader(HttpHeaders.SET_COOKIE);
  }

  @Test
  @DisplayName("인증된 사용자가 로그아웃 시 200 OK와 함께 쿠키 Max-Age가 0으로 설정되어야 한다.")
  @WithMockUser(username = "test@example.com", authorities = "ADMIN")
  void logout_success() {
    // Given authenticated user(@WithMockUser)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    assertThat(userDetails.getUsername()).isEqualTo("test@example.com");

    // When logout
    mvc.post().uri("/api/auth/logout")
        .with(csrf()).assertThat()
        // Then
        .hasStatus(HttpStatus.OK)
        .headers().hasEntrySatisfying(HttpHeaders.SET_COOKIE, values -> {
          values.forEach(v -> assertThat(v).contains("Max-Age=0"));
        });
  }

  @Test
  @DisplayName("인증되지 않은 상태에서 로그아웃 시 401 응답이 반환되어야 한다.")
  void logout_unauthenticated() {
    // When & Then
    assertThat(mvc.post().uri("/api/auth/logout")
        .with(csrf()))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  private String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
