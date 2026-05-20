package io.github.hgkimer.blog.support.web.controller;

import io.github.hgkimer.blog.properties.JwtProperties;
import io.github.hgkimer.blog.security.CustomUserDetailsService;
import io.github.hgkimer.blog.security.JwtAuthenticationFilter;
import io.github.hgkimer.blog.security.JwtTokenProvider;
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
