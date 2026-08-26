package com.nexusbank.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI nexusBankAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexusBank Auth Service API")
                        .version("v1")
                        .description("Authentication and authorization APIs for the NexusBank digital banking platform.")
                        .contact(new Contact()
                                .name("NexusBank Engineering")
                                .email("engineering@nexusbank.local"))
                        .license(new License()
                                .name("Proprietary")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}