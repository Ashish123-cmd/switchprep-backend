package com.switchmate.jobs.matching;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.switchmate.domain.enums.JobSource;

public record JobResponse(
		UUID id,
		JobSource source,
		String externalId,
		String title,
		String company,
		String location,
		String applyUrl,
		String description,
		Integer minExperience,
		Integer maxExperience,
		Instant postedAt,
		Set<String> skills) {
}