package com.estradahermanos.shoestock.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    private static final String AUTH_TOKEN_SCHEME = "auth-token";

    @Bean
    public OpenAPI openAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("EH Shoe Stock API")
                        .description("Inventory control API for Estrada Hermanos shoe store")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(AUTH_TOKEN_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(AUTH_TOKEN_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(AUTH_TOKEN_SCHEME)));
    }
}
