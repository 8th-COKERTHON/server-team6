package com.team6.server.global.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig {
 @Bean OpenAPI openAPI(){String scheme="BearerAuth";return new OpenAPI().info(new Info().title("Team 6 API").description("Hackathon backend API").version("v1")).addSecurityItem(new SecurityRequirement().addList(scheme)).components(new Components().addSecuritySchemes(scheme,new SecurityScheme().name(scheme).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));}
}
