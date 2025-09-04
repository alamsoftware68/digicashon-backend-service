package com.repairapp.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Swagger documentation
 */
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort + "/api").description("Development Server"),
                new Server().url("https://api.repairapp.com/api").description("Production Server")
            ))
            .info(new Info()
                .title("DigiCashOn Repair Service API")
                .description("Backend API for DigiCashOn Repair Service Application - A comprehensive laptop, mobile, tablet and smartwatch repair service platform")
                .version("1.0.0")
                .contact(new Contact()
                    .name("RepairApp Development Team")
                    .email("dev@repairapp.com")
                    .url("https://repairapp.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"))
            );
    }
}
