package com.iemr.tm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Arrays;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    private static final String DEFAULT_SERVER_URL = "http://localhost:9090";

    @Bean
    public OpenAPI customOpenAPI(Environment env) {
        String devUrl = env.getProperty("api.dev.url", DEFAULT_SERVER_URL);
        String uatUrl = env.getProperty("api.uat.url", DEFAULT_SERVER_URL);
        String demoUrl = env.getProperty("api.demo.url", DEFAULT_SERVER_URL);
        return new OpenAPI()
            .info(new Info().title("Scheduler API").version("1.0.0").description("A microservice for scheduling and appointment management."))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().name("bearerAuth").type(SecurityScheme.Type.HTTP).scheme("bearer")))
            .servers(Arrays.asList(
                new Server().url(devUrl).description("Dev"),
                new Server().url(uatUrl).description("UAT"),
                new Server().url(demoUrl).description("Demo")
            ));
    }

}
