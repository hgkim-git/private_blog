package io.github.hgkimer.blog.support.web.controller;

import io.github.hgkimer.blog.config.SecurityConfig;
import io.github.hgkimer.blog.config.WebMvcConfig;
import io.github.hgkimer.blog.security.JwtAuthenticationFilter;
import io.github.hgkimer.blog.web.interceptor.VisitInterceptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public @interface ControllerSliceTest {

  @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
  Class<?>[] value() default {};

  @AliasFor(annotation = WebMvcTest.class, attribute = "excludeFilters")
  ComponentScan.Filter[] excludeFilters() default {
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
          SecurityConfig.class,
          JwtAuthenticationFilter.class,
          WebMvcConfig.class,
          VisitInterceptor.class
      })
  };
}
