package com.recipebox.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recipeBoxOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RecipeBox – Personal Recipe and Meal Planner API")
                        .description("REST API for home cooks and hostel residents to store recipes, plan weekly meals, and generate consolidated shopping lists.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RecipeBox Engineering Team")
                                .email("support@recipebox.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
