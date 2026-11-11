package com.switchmate.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
@ActiveProfiles({"test", "fake-ai"})
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
		registry.add("app.auth.signup-draft.ttl-minutes", () -> "30");
		registry.add("app.rate-limit.signup-resume.max-requests", () -> "100");
		registry.add("app.rate-limit.signup-resume.window-minutes", () -> "10");
	}

	@Test
	void completeSignupWithDraftThenLoginAndGetCurrentUser() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"resume.pdf",
				"application/pdf",
				ResumeTestFiles.pdfWithText("Fake resume content"));
		MvcResult upload = mockMvc.perform(multipart("/api/auth/signup/resume")
				.file(file)
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isCreated())
			.andReturn();
		String draftId = objectMapper.readTree(upload.getResponse().getContentAsString())
				.path("draftId").asText();

		String email = "auth-flow@example.com";
		String password = "SecurePass123";
		MvcResult complete = mockMvc.perform(post("/api/auth/signup/complete")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"draftId", draftId,
						"name", "Test User",
						"email", email,
						"password", password))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.user.email").value(email))
			.andExpect(jsonPath("$.user.profile.onboardingCompleted").value(false))
			.andReturn();
		JsonNode completeBody = objectMapper.readTree(complete.getResponse().getContentAsString());

		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.id").value(completeBody.path("user").path("id").asText()))
			.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(completeBody.path("user").path("id").asText()))
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.profile.onboardingCompleted").value(false));
	}
}