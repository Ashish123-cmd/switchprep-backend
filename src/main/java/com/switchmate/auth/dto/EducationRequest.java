package com.switchmate.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EducationRequest(
		@NotBlank @Size(max = 255) String institution,
		@Size(max = 255) String degree,
		@Size(max = 255) String field,
		@Positive Integer startYear,
		@Positive Integer endYear) {
}