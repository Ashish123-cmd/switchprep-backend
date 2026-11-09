package com.switchmate.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Email @Size(max = 320) String email,
		@Size(max = 50) String phone,
		@Size(max = 255) String location,
		@Size(max = 255) String headline,
		String summary,
		@PositiveOrZero Integer totalExperienceYears,
		@NotNull Set<@NotBlank @Size(max = 255) String> skills,
		@NotNull List<@Valid ExperienceRequest> experiences,
		@NotNull List<@Valid EducationRequest> educations,
		@Size(max = 255) String resumeFileName,
		Instant resumeUploadedAt) {
}