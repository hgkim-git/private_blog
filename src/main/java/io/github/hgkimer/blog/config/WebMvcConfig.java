package io.github.hgkimer.blog.config;

import io.github.hgkimer.blog.web.interceptor.VisitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final VisitInterceptor visitInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(visitInterceptor)
        .addPathPatterns("/", "/posts", "/posts/**")
        .excludePathPatterns("/admin/**", "/api/**", "/css/**", "/js/**", "/img/**");
  }
}
