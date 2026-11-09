package com.switchmate.jobs.ingestion;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.switchmate.domain.enums.JobSourceType;

@Component
public class AshbyJobSource implements JobSource {

	private static final Logger logger = LoggerFactory.getLogger(AshbyJobSource.class);
	private static final String API_BASE_URL = "https://api.ashbyhq.com";

	private final JobSourceHttpClient httpClient;
	private final List<String> boards;
	private final SkillExtractor skillExtractor;
	private final ExperienceRangeExtractor experienceRangeExtractor;

	public AshbyJobSource(
			JobSourceHttpClient httpClient,
			@Value("${app.jobs.ashby.boards:}") String configuredBoards,
			SkillExtractor skillExtractor,
			ExperienceRangeExtractor experienceRangeExtractor) {
		this.httpClient = httpClient;
		this.boards = Arrays.stream(configuredBoards.split(","))
				.map(String::strip)
				.filter(board -> !board.isEmpty())
				.toList();
		this.skillExtractor = skillExtractor;
		this.experienceRangeExtractor = experienceRangeExtractor;
	}

	@Override
	public JobSourceType type() {
		return JobSourceType.ASHBY;
	}

	@Override
	public List<RawJob> fetchJobs() {
		if (boards.isEmpty()) {
			throw new IncompleteJobFetchException("No Ashby board identifiers are configured", List.of());
		}
		List<RawJob> jobs = new ArrayList<>();
		List<String> failedBoards = new ArrayList<>();
		for (String board : boards) {
			try {
				jobs.addAll(fetchBoard(board));
			} catch (RuntimeException exception) {
				logger.warn("Ashby postings fetch failed for {}: {}", board, exception.getMessage());
				failedBoards.add(board);
			}
		}
		if (!failedBoards.isEmpty()) {
			throw new IncompleteJobFetchException(
					"Ashby fetch incomplete for boards: " + String.join(", ", failedBoards), jobs);
		}
		return List.copyOf(jobs);
	}

	private List<RawJob> fetchBoard(String board) {
		URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
				.pathSegment("posting-api", "job-board", board)
				.build()
				.encode()
				.toUri();
		JsonNode response = httpClient.getJson(uri);
		if (!response.path("jobs").isArray()) {
			throw new IllegalStateException("Ashby returned an invalid job board response");
		}
		List<RawJob> jobs = new ArrayList<>();
		for (JsonNode job : response.path("jobs")) {
			if (job.has("isListed") && !job.path("isListed").asBoolean()) {
				continue;
			}
			String stableId = firstNonBlank(
					job.path("id").asText(null),
					firstNonBlank(job.path("jobUrl").asText(null), job.path("applyUrl").asText(null)));
			String title = clean(job.path("title").asText(null));
			if (stableId == null || title == null) {
				continue;
			}
			String description = clean(job.path("descriptionPlain").asText(null));
			if (description == null) {
				description = clean(job.path("descriptionHtml").asText(null));
			}
			ExperienceRange range = experienceRangeExtractor.extract(title + " " + (description == null ? "" : description));
			jobs.add(new RawJob(
					board + ":" + stableId,
					title,
					board,
					clean(job.path("location").asText(null)),
					firstNonBlank(job.path("applyUrl").asText(null), job.path("jobUrl").asText(null)),
					description,
					List.copyOf(skillExtractor.extract(title, description)),
					range == null ? null : range.minExperience(),
					range == null ? null : range.maxExperience(),
				parseTimestamp(job.path("publishedAt"))));
		}
		return jobs;
	}

	private Instant parseTimestamp(JsonNode timestamp) {
		String value = timestamp.asText(null);
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return OffsetDateTime.parse(value).toInstant();
		} catch (DateTimeParseException exception) {
			logger.debug("Ignoring invalid Ashby publication time {}", value);
			return null;
		}
	}

	private String firstNonBlank(String first, String fallback) {
		String value = clean(first);
		return value == null ? clean(fallback) : value;
	}

	private String clean(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.strip().replaceAll("\\s+", " ");
	}
}