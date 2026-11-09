package com.switchmate.jobs.matching;

import java.util.List;

public record JobMatchDetails(
		JobResponse job,
		int score,
		List<String> reasons,
		boolean saved,
		boolean hidden,
		List<String> matchedSkills,
		List<String> missingSkills) {
}