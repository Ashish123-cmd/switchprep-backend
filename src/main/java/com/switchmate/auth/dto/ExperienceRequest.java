package com.switchmate.auth.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExperienceRequest(
		@NotBlank @Size(max = 255) String company,
		@NotBlank @Size(max = 255) String title,
		@NotNull LocalDate startDate,
		LocalDate endDate,
		String description) {
}