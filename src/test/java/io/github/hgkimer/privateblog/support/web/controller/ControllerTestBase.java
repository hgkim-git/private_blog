package io.github.hgkimer.privateblog.support.web.controller;

import io.github.hgkimer.privateblog.properties.JwtProperties;
import io.github.hgkimer.privateblog.security.CustomUserDetailsService;
import io.github.hgkimer.privateblog.security.JwtAuthenticationFilter;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ControllerSliceTest
public abstract class ControllerTestBase {

  @MockitoBean
  protected JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean
  protected CustomUserDetailsService customUserDetailsService;
  @MockitoBean
  protected JwtTokenProvider jwtTokenProvider;
  @MockitoBean
  protected JwtProperties jwtProperties;
}
