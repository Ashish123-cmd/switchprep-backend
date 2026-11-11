package com.switchmate.auth.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class GoogleAuthResponseTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void serializesNewUserFlagAsIsNewUser() throws Exception {
		GoogleAuthResponse response = new GoogleAuthResponse(
				"access-token",
				new UserResponse(UUID.randomUUID(), "Taylor", "person@example.com", new ProfileResponse(false)),
				true);

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

		assertTrue(json.has("token"));
		assertTrue(json.has("user"));
		assertTrue(json.has("isNewUser"), "JSON must expose isNewUser: " + json);
	}
}
