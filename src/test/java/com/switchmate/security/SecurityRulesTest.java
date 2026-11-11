package com.switchmate.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pins the public surface of the API: only login, the two signup steps, Google
 * sign-in, and the health endpoint may be called without credentials.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("fake-ai")
class SecurityRulesTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publicAuthEndpointsAreReachableWithoutJwt() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/auth/signup/complete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/auth/google")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());

		mockMvc.perform(multipart("/api/auth/signup/resume")
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isBadRequest());
	}

	@Test
	void healthEndpointIsPublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk());
	}

	@Test
	void protectedEndpointsRequireJwt() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(get("/api/jobs"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/profile"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/admin/jobs/ingest"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void apiDocumentationIsNotPublicByDefault() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/swagger-ui/index.html"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void adminIngestionRejectsAnInvalidApiKey() throws Exception {
		mockMvc.perform(post("/api/admin/jobs/ingest")
				.header(AdminApiKeyAuthenticationFilter.ADMIN_API_KEY_HEADER, "not-the-configured-key"))
			.andExpect(status().isUnauthorized());
	}
}
