package io.github.hgkimer.privateblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PrivateBlogApplication {

  public static void main(String[] args) {
    SpringApplication.run(PrivateBlogApplication.class, args);
  }

}
