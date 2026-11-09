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
public class LeverJobSource implements JobSource {

	private static final Logger logger = LoggerFactory.getLogger(LeverJobSource.class);
	private static final String API_BASE_URL = "https://api.lever.co";

	private final JobSourceHttpClient httpClient;
	private final List<String> companies;
	private final SkillExtractor skillExtractor;
	private final ExperienceRangeExtractor experienceRangeExtractor;

	public LeverJobSource(
			JobSourceHttpClient httpClient,
			@Value("${app.jobs.lever.companies:}") String configuredCompanies,
			SkillExtractor skillExtractor,
			ExperienceRangeExtractor experienceRangeExtractor) {
		this.httpClient = httpClient;
		this.companies = Arrays.stream(configuredCompanies.split(","))
				.map(String::strip)
				.filter(company -> !company.isEmpty())
				.toList();
		this.skillExtractor = skillExtractor;
		this.experienceRangeExtractor = experienceRangeExtractor;
	}

	@Override
	public JobSourceType type() {
		return JobSourceType.LEVER;
	}

	@Override
	public List<RawJob> fetchJobs() {
		if (companies.isEmpty()) {
			throw new IncompleteJobFetchException("No Lever company identifiers are configured", List.of());
		}
		List<RawJob> jobs = new ArrayList<>();
		List<String> failedCompanies = new ArrayList<>();
		for (String company : companies) {
			try {
				jobs.addAll(fetchCompany(company));
			} catch (RuntimeException exception) {
				logger.warn("Lever postings fetch failed for {}: {}", company, exception.getMessage());
				failedCompanies.add(company);
			}
		}
		if (!failedCompanies.isEmpty()) {
			throw new IncompleteJobFetchException(
					"Lever fetch incomplete for companies: " + String.join(", ", failedCompanies), jobs);
		}
		return List.copyOf(jobs);
	}

	private List<RawJob> fetchCompany(String company) {
		URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
				.pathSegment("v0", "postings", company)
				.queryParam("mode", "json")
				.build()
				.encode()
				.toUri();
		JsonNode response = httpClient.getJson(uri);
		if (!response.isArray()) {
			throw new IllegalStateException("Lever returned an invalid postings response");
		}
		List<RawJob> jobs = new ArrayList<>();
		for (JsonNode posting : response) {
			String id = clean(posting.path("id").asText(null));
			String title = clean(posting.path("text").asText(null));
			if (id == null || title == null) {
				continue;
			}
			String description = clean(posting.path("descriptionPlain").asText(null));
			if (description == null) {
				description = clean(posting.path("description").asText(null));
			}
			ExperienceRange range = experienceRangeExtractor.extract(title + " " + (description == null ? "" : description));
			jobs.add(new RawJob(
					company + ":" + id,
					title,
					company,
					clean(posting.path("categories").path("location").asText(null)),
					firstNonBlank(posting.path("applyUrl").asText(null), posting.path("hostedUrl").asText(null)),
					description,
					List.copyOf(skillExtractor.extract(title, description)),
					range == null ? null : range.minExperience(),
					range == null ? null : range.maxExperience(),
				parseTimestamp(posting.path("createdAt"))));
		}
		return jobs;
	}

	private Instant parseTimestamp(JsonNode timestamp) {
		if (timestamp.isNumber()) {
			return Instant.ofEpochMilli(timestamp.asLong());
		}
		String value = timestamp.asText(null);
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Instant.parse(value);
		} catch (DateTimeParseException ignored) {
			try {
				return OffsetDateTime.parse(value).toInstant();
			} catch (DateTimeParseException exception) {
				logger.debug("Ignoring invalid Lever posting timestamp {}", value);
				return null;
			}
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