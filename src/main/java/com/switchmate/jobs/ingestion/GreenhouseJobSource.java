package com.switchmate.jobs.ingestion;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.switchmate.domain.enums.JobSourceType;

@Component
public class GreenhouseJobSource implements JobSource {

	private static final Logger logger = LoggerFactory.getLogger(GreenhouseJobSource.class);
	private static final String API_BASE_URL = "https://boards-api.greenhouse.io";

	private final RestClient restClient;
	private final List<String> boardTokens;
	private final SkillExtractor skillExtractor;
	private final ExperienceRangeExtractor experienceRangeExtractor;

	public GreenhouseJobSource(
			RestClient.Builder restClientBuilder,
			@Value("${app.jobs.greenhouse.board-tokens:}") String configuredBoardTokens,
			SkillExtractor skillExtractor,
			ExperienceRangeExtractor experienceRangeExtractor) {
		this.restClient = restClientBuilder.build();
		this.boardTokens = Arrays.stream(configuredBoardTokens.split(","))
				.map(String::strip)
				.filter(token -> !token.isEmpty())
				.toList();
		this.skillExtractor = skillExtractor;
		this.experienceRangeExtractor = experienceRangeExtractor;
	}

	@Override
	public JobSourceType type() {
		return JobSourceType.GREENHOUSE;
	}

	@Override
	public List<RawJob> fetchJobs() {
		if (boardTokens.isEmpty()) {
			throw new IncompleteJobFetchException("No Greenhouse board tokens are configured", List.of());
		}
		List<RawJob> jobs = new ArrayList<>();
		List<String> failedBoards = new ArrayList<>();
		for (String boardToken : boardTokens) {
			try {
				jobs.addAll(fetchBoard(boardToken));
			} catch (RuntimeException exception) {
				logger.warn("Greenhouse board fetch failed for {}: {}", boardToken, exception.getMessage());
				failedBoards.add(boardToken);
			}
		}
		if (!failedBoards.isEmpty()) {
			throw new IncompleteJobFetchException(
					"Greenhouse fetch incomplete for boards: " + String.join(", ", failedBoards), jobs);
		}
		return List.copyOf(jobs);
	}

	private List<RawJob> fetchBoard(String boardToken) {
		URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
				.pathSegment("v1", "boards", boardToken, "jobs")
				.queryParam("content", "true")
				.build()
				.encode()
				.toUri();
		JsonNode response = restClient.get().uri(uri).retrieve().body(JsonNode.class);
		if (response == null || !response.path("jobs").isArray()) {
			throw new IllegalStateException("Greenhouse returned an invalid jobs response");
		}
		List<RawJob> jobs = new ArrayList<>();
		for (JsonNode job : response.path("jobs")) {
			String sourceId = job.path("id").asText(null);
			String title = clean(job.path("title").asText(null));
			if (sourceId == null || title == null) {
				continue;
			}
			String description = job.path("content").asText("");
			String company = clean(job.path("company_name").asText(null));
			String location = clean(job.path("location").path("name").asText(null));
			if (location == null && job.path("offices").isArray() && !job.path("offices").isEmpty()) {
				location = clean(job.path("offices").get(0).path("location").asText(null));
			}
			ExperienceRange range = experienceRangeExtractor.extract(title + " " + description);
			jobs.add(new RawJob(
					boardToken + ":" + sourceId,
					title,
					company == null ? boardToken : company,
					location,
					clean(job.path("absolute_url").asText(null)),
					description,
					List.copyOf(skillExtractor.extract(title, description)),
					range == null ? null : range.minExperience(),
					range == null ? null : range.maxExperience(),
					parsePostedAt(job)));
		}
		return jobs;
	}

	private Instant parsePostedAt(JsonNode job) {
		String publishedAt = job.path("first_published").asText(null);
		if (publishedAt == null || publishedAt.isBlank()) {
			publishedAt = job.path("updated_at").asText(null);
		}
		if (publishedAt == null || publishedAt.isBlank()) {
			return null;
		}
		try {
			return OffsetDateTime.parse(publishedAt).toInstant();
		} catch (DateTimeParseException exception) {
			logger.debug("Ignoring invalid Greenhouse publication time {}", publishedAt);
			return null;
		}
	}

	private String clean(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.strip().replaceAll("\\s+", " ");
	}
}