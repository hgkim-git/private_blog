package io.github.hgkimer.blog.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  @Bean
  public LettuceClientConfigurationBuilderCustomizer lettuceClientOptions() {
    return builder -> builder.clientOptions(
        ClientOptions.builder()
            .pingBeforeActivateConnection(true)
            .autoReconnect(true)
            .socketOptions(SocketOptions.builder()
                .keepAlive(true)
                .build())
            .build()
    );
  }
}
