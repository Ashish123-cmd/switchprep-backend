package com.switchmate.jobs.ingestion;

import java.time.Instant;
import java.util.List;

public record RawJob(
		String externalId,
		String title,
		String company,
		String location,
		String applyUrl,
		String description,
		List<String> skills,
		Integer minExperience,
		Integer maxExperience,
		Instant postedAt) {
}