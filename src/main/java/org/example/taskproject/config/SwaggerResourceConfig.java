package org.example.taskproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/openapi.yaml")
                .addResourceLocations("classpath:/docs/openapi.yaml");
    }
}


//http://localhost:8080/swagger-ui/index.html?url=/docs/openapi.yaml