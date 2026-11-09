package com.switchmate.auth.dto;

import java.util.Map;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StandardAnswersRequest(
		Boolean requiresSponsorship,
		Boolean willingToRelocate,
		@URL(protocol = "https") @Size(max = 2048) String linkedinUrl,
		@URL(protocol = "https") @Size(max = 2048) String githubUrl,
		@URL(protocol = "https") @Size(max = 2048) String portfolioUrl,
		Map<@NotBlank @Size(max = 1000) String, @NotNull String> customAnswers) {
}