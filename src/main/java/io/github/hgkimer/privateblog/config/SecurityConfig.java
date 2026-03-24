package io.github.hgkimer.privateblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    http
        .csrf(CsrfConfigurer::disable)
        // Session STATELESS 설정
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
