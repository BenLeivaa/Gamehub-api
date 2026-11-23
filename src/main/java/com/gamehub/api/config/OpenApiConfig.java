package com.gamehub.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger: http://localhost:8080/swagger-ui.html */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "GameHub API",
        version = "1.0",
        description = "API REST de GameHub Store: productos, categorías, usuarios y boletas, con autenticación JWT y roles."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
