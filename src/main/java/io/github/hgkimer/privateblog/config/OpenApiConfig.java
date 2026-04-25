package io.github.hgkimer.privateblog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "HGKimer Blog API",
        version = "1.0.0",
        description = "Private blog API documentation"
    )
)
@Configuration
public class OpenApiConfig {

}
