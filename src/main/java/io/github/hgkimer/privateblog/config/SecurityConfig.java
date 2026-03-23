package io.github.hgkimer.privateblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hgkimer.privateblog.security.CsrfCookieFilter;
import io.github.hgkimer.privateblog.security.JwtAuthenticationFilter;
import io.github.hgkimer.privateblog.service.CustomUserDetailsService;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

// Spring Security 6부터 @EnableWebSecurity 에 @Configuration이 포함되지 않아 별도로 표기 필요
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService customUserDetailsService;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // CSRF 토큰 핸들러 설정: _csrf 속성 명시를 피하기 위해 name을 null로 설정
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName(null);

    http
        // 쿠키에 JWT 토큰을 저장할 예정이므로 보안을 위해 csrf 활성화
        .csrf(csrf -> csrf
            // 쿠키 방식에서는 CsrfTokenRequestAttributeHandler를 써야 JS가 쿠키를 그대로 헤더에 넣는 단순한 방식으로 동작
            // JS에서 읽을 수 있도록 HttpOnly=false 쿠키로 CSRF 토큰 발급(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(requestHandler)
            .ignoringRequestMatchers("/api/auth/login")
            // Spring Security 6.1+ 에서는 csrf 설정 내에서도 sessionAuthenticationStrategy를 설정할 수 있음
            // STATELESS 세션 정책을 사용하더라도 CsrfConfigurer가 기본으로 추가하는 CsrfAuthenticationStrategy가
            // 인증 시마다 기존 CSRF 토큰 쿠키를 삭제하고 재발급하는 것을 방지
            .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
        )
        // Session STATELESS 설정
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            // sessionAuthenticationStrategy를 NullAuthenticatedSessionStrategy로 명시 설정
            .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
        )
        .authorizeHttpRequests(
            authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers("/admin/login", "/api/auth/login", "/css/**", "/js/**",
                        "/img/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin/**").hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.POST).hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.PATCH).hasAuthority("ADMIN")
                    .requestMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")
                    .anyRequest().permitAll()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(ErrorCode.UNAUTHORIZED_ACCESS.getHttpStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED_ACCESS);
                String json = objectMapper.writeValueAsString(errorResponse);

                PrintWriter writer = response.getWriter();
                writer.print(json);
                writer.flush();
              } else {
                response.sendRedirect("/admin/login");
              }
            })
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // CsrfFilter 이후에 실행하여 XSRF-TOKEN 쿠키가 매 응답에 항상 set되도록 강제
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        // 커스텀 /api/auth/logout 을 사용하므로 Spring Security 기본 LogoutFilter 비활성화
        // 기본 LogoutFilter는 CsrfLogoutHandler를 포함하여 POST /logout 요청 시 XSRF-TOKEN 쿠키를
        // Max-Age=0 으로 삭제한다. 비활성화하지 않으면 의도치 않게 CSRF 쿠키가 삭제될 수 있다.
        .logout(LogoutConfigurer::disable)
    ;
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /*
    SpringBoot는 UserDetailsService, PasswordEncoder가 Bean 으로 등록되어 있으면
    자동으로 DaoAuthenticationProvider를 생성하여 사용한다.

    다만 Bean으로는 자동 등록되지 않아 추가 설정을 하려면 아래처럼 명시적인 Bean 설정이 필요하다.
    이 프로젝트에서는 사실상 추가 설정은 필요없지만 UserDetailsService의 구현체와,

    PasswordEncoder의 구현체를 명시적으로 Bean 등록하여 구성을 보여주기 위하여 선언.
  */
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

}
