package com.estradahermanos.shoestock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    @Bean
    public OpenAPI openAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("EH Shoe Stock API")
                        .description("Inventory control API for Estrada Hermanos shoe store")
                        .version("1.0.0"));
    }
}
