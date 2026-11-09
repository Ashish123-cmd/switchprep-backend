package com.switchmate.jobs.matching;

import java.util.Set;

import com.switchmate.domain.enums.JobSource;

public record JobSearchCriteria(
		String q,
		Set<JobSource> sources,
		String location,
		Integer minExperience,
		Integer maxExperience,
		Integer minScore,
		boolean savedOnly) {
}