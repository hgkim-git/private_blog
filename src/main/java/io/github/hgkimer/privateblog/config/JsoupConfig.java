package io.github.hgkimer.privateblog.config;

import java.util.stream.IntStream;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsoupConfig {

  @Bean
  Safelist safelist() {
    Safelist safelist = Safelist.relaxed();
    // <h1> ~ <h6> for Table of Contents
    IntStream.range(1, 7)
        .forEach(i -> safelist.addAttributes("h" + i, "id"));
    // for Highlight.js code block decorating
    safelist.addAttributes("code", "class");
    return safelist;
  }
}
