package com.switchmate.resume;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchmate.common.exception.ResumeParsingException;

@Component
@Profile("!fake-ai")
public class OpenAiResumeParser implements ResumeParser {

	private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
	private static final String SYSTEM_PROMPT = """
			Extract the candidate profile from the resume text. Return exactly one strict JSON object, with no markdown or extra text, matching this shape:
			{"name":string|null,"phone":string|null,"location":string|null,"headline":string|null,"summary":string|null,"totalExperienceYears":number|null,"skills":string[],"experiences":[{"company":string|null,"title":string|null,"startDate":"YYYY-MM-DD"|null,"endDate":"YYYY-MM-DD"|null,"description":string|null}],"educations":[{"institution":string|null,"degree":string|null,"field":string|null,"startYear":number|null,"endYear":number|null}]}
			Use null for unknown scalar values and empty arrays for missing lists. Do not infer facts not present in the resume.
			""";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String apiKey;
	private final String model;

	public OpenAiResumeParser(
			RestClient.Builder restClientBuilder,
			ObjectMapper objectMapper,
			@Value("${app.openai.api-key:}") String apiKey,
			@Value("${app.openai.model:gpt-4o-mini}") String model) {
		this.restClient = restClientBuilder.build();
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
		this.model = model;
	}

	@Override
	public ParsedProfile parse(String resumeText) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new ResumeParsingException("Resume parsing is not configured; set OPENAI_API_KEY or activate fake-ai");
		}

		Map<String, Object> request = Map.of(
				"model", model,
				"messages", List.of(
						Map.of("role", "system", "content", SYSTEM_PROMPT),
						Map.of("role", "user", "content", resumeText)),
				"response_format", Map.of("type", "json_object"));
		try {
			JsonNode response = restClient.post()
					.uri(CHAT_COMPLETIONS_URL)
					.header("Authorization", "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(JsonNode.class);
			if (response == null || !response.path("choices").isArray() || response.path("choices").isEmpty()) {
				throw new ResumeParsingException("The resume parser returned an empty response");
			}
			String json = response.path("choices").get(0).path("message").path("content").asText(null);
			if (json == null || json.isBlank()) {
				throw new ResumeParsingException("The resume parser returned no profile data");
			}
			return objectMapper.readerFor(ParsedProfile.class)
					.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
					.readValue(json);
		} catch (ResumeParsingException exception) {
			throw exception;
		} catch (RestClientException | JsonProcessingException exception) {
			throw new ResumeParsingException("The resume parser returned invalid profile JSON", exception);
		}
	}
}