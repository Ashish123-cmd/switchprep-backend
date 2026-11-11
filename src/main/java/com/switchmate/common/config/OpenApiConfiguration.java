package com.switchmate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

	public static final String BEARER_AUTH = "bearerAuth";
	public static final String ADMIN_API_KEY_AUTH = "adminApiKey";

	@Bean
	OpenAPI switchmateOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Switchmate API")
						.version("v1")
						.description("Authentication, profile, resume parsing, job matching, tracker, and auto-apply API. "
								+ "Only POST /api/auth/login, POST /api/auth/signup/resume, "
								+ "POST /api/auth/signup/complete, POST /api/auth/google and the health endpoint "
								+ "are public; every other endpoint requires the bearer token returned by the auth "
								+ "endpoints."))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT access token returned by /api/auth/login, "
										+ "/api/auth/signup/complete, or /api/auth/google"))
						.addSecuritySchemes(ADMIN_API_KEY_AUTH, new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.name("X-Admin-Api-Key")
								.description("Admin API key used to trigger job ingestion")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
