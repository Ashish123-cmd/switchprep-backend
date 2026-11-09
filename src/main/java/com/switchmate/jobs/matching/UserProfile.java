package com.switchmate.jobs.matching;

import java.util.Set;

public record UserProfile(
		String headline,
		String location,
		Integer totalExperienceYears,
		Set<String> skills) {
}