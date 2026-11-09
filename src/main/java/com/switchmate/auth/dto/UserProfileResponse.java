package com.switchmate.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record UserProfileResponse(
		String name,
		String email,
		boolean onboardingCompleted,
		String phone,
		String location,
		String headline,
		String summary,
		Integer totalExperienceYears,
		Set<String> skills,
		List<ExperienceResponse> experiences,
		List<EducationResponse> educations,
		String resumeFileName,
		Instant resumeUploadedAt) {
}