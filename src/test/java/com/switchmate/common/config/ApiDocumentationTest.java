package com.switchmate.common.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchmate.auth.dto.GoogleAuthRequest;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.SignupCompleteRequest;
import com.switchmate.auth.web.AuthController;
import com.switchmate.security.CurrentUser;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guards the OpenAPI contract: the JWT scheme must be declared globally, the
 * public auth endpoints must opt out of it, and the admin key scheme must be
 * documented for ingestion.
 */
@SpringBootTest(properties = "app.security.public-api-docs=true")
@AutoConfigureMockMvc
@ActiveProfiles("fake-ai")
class ApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void openApiDocumentDeclaresSecuritySchemesAndPublicEndpoints() throws Exception {
		MvcResult result = mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode document = objectMapper.readTree(result.getResponse().getContentAsString());

		assertTrue(document.at("/components/securitySchemes/bearerAuth").isObject(),
				"bearerAuth scheme must be documented");
		assertTrue(document.at("/components/securitySchemes/adminApiKey").isObject(),
				"adminApiKey scheme must be documented");

		JsonNode loginSecurity = document.at("/paths/~1api~1auth~1login/post/security");
		assertTrue(loginSecurity.isMissingNode() || loginSecurity.isEmpty(),
				"login must opt out of the global bearer token: " + loginSecurity);

		assertTrue(document.at("/security/0").has("bearerAuth"),
				"the bearer token must be the document-level security requirement");

		assertTrue(document.at("/paths/~1api~1auth~1signup~1resume/post").isObject(),
				"signup/resume must be documented");
		assertTrue(document.at("/paths/~1api~1auth~1signup~1complete/post").isObject(),
				"signup/complete must be documented");
		assertTrue(document.at("/paths/~1api~1auth~1google/post").isObject(),
				"google sign-in must be documented");
		assertTrue(document.at("/paths/~1api~1auth~1signup/post").isMissingNode(),
				"the removed POST /api/auth/signup must not be documented");
	}

	@Test
	void onlyPublicAuthEndpointsOptOutOfTheBearerRequirement() throws Exception {
		assertNotNull(AuthController.class
				.getMethod("uploadSignupResume", MultipartFile.class, HttpServletRequest.class)
				.getAnnotation(SecurityRequirements.class));
		assertNotNull(AuthController.class
				.getMethod("completeSignup", SignupCompleteRequest.class)
				.getAnnotation(SecurityRequirements.class));
		assertNotNull(AuthController.class
				.getMethod("login", LoginRequest.class)
				.getAnnotation(SecurityRequirements.class));
		assertNotNull(AuthController.class
				.getMethod("googleLogin", GoogleAuthRequest.class)
				.getAnnotation(SecurityRequirements.class));
		assertNull(AuthController.class
				.getMethod("currentUser", CurrentUser.class)
				.getAnnotation(SecurityRequirements.class));
	}
}
