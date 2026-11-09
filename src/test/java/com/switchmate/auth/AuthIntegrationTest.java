package com.switchmate.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("TEST_DB_URL", postgres::getJdbcUrl);
		registry.add("TEST_DB_USERNAME", postgres::getUsername);
		registry.add("TEST_DB_PASSWORD", postgres::getPassword);
		registry.add("TEST_FRONTEND_ORIGIN", () -> "http://localhost:5173");
		registry.add("JWT_SECRET", () -> Base64.getEncoder().encodeToString(
				"switchmate-test-secret-key-32bytes".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void signupLoginAndGetCurrentUser() throws Exception {
		String email = "auth-flow@example.com";
		String password = "SecurePass123";
		MvcResult signup = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Test User","email":"auth-flow@example.com","password":"SecurePass123"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.user.profile.onboardingCompleted").value(false))
			.andReturn();
		JsonNode signupBody = objectMapper.readTree(signup.getResponse().getContentAsString());

		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(java.util.Map.of("email", email, "password", password))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.id").value(signupBody.path("user").path("id").asText()))
			.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(signupBody.path("user").path("id").asText()))
				.andExpect(jsonPath("$.profile.onboardingCompleted").value(false));
	}
}