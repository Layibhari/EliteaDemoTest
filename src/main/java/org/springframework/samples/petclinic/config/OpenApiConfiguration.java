package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		return new OpenAPI()
			.info(new Info().title("Spring PetClinic REST API")
				.description("API REST modernizada usando el patrón Strangler Fig para coexistir con Thymeleaf.")
				.version("v1.0"))
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
			.components(new Components().addSecuritySchemes(securitySchemeName,
					new SecurityScheme().name(securitySchemeName)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")));
	}

}
