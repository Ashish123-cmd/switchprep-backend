package com.switchmate.auth.dto;

import java.util.Map;

public record StandardAnswersResponse(
		Boolean requiresSponsorship,
		Boolean willingToRelocate,
		String linkedinUrl,
		String githubUrl,
		String portfolioUrl,
		Map<String, String> customAnswers) {
}