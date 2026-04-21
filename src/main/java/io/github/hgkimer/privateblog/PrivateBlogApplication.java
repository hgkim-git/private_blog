package io.github.hgkimer.privateblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PrivateBlogApplication {

  public static void main(String[] args) {
    SpringApplication.run(PrivateBlogApplication.class, args);
  }

}
