package com.switchmate.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchmate.common.exception.ResumeParsingException;

class OpenAiResumeParserTest {

	private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

	@Test
	void parsesStructuredJsonFromChatCompletion() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenAiResumeParser parser = parserWithOpenAiConfig(builder, objectMapper);
		String profileJson = """
				{"name":"Sam Lee","phone":null,"location":"Toronto","headline":"Engineer","summary":"Backend engineer","totalExperienceYears":4,"skills":["Java"],"experiences":[],"educations":[]}
				""";
		String completion = objectMapper.writeValueAsString(Map.of(
				"choices", List.of(Map.of("message", Map.of("content", profileJson)))));

		server.expect(requestTo(COMPLETIONS_URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("Authorization", "Bearer test-api-key"))
				.andRespond(withSuccess(completion, MediaType.APPLICATION_JSON));

		ParsedProfile result = parser.parse("resume text");

		assertEquals("Sam Lee", result.name());
		assertEquals(List.of("Java"), result.skills());
		server.verify();
	}

	@Test
	void rejectsInvalidJsonReturnedByChatCompletion() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		OpenAiResumeParser parser = parserWithOpenAiConfig(builder, objectMapper);
		String completion = objectMapper.writeValueAsString(Map.of(
				"choices", List.of(Map.of("message", Map.of("content", "{invalid json")))));

		server.expect(requestTo(COMPLETIONS_URL))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(completion, MediaType.APPLICATION_JSON));

		assertThrows(ResumeParsingException.class, () -> parser.parse("resume text"));
		server.verify();
	}

	private OpenAiResumeParser parserWithOpenAiConfig(RestClient.Builder builder, ObjectMapper objectMapper) {
		return new OpenAiResumeParser(
				builder,
				objectMapper,
				"test-api-key",
				"",
				COMPLETIONS_URL,
				"https://openrouter.ai/api/v1/chat/completions",
				"test-model",
				"openai/gpt-4o-mini");
	}
}