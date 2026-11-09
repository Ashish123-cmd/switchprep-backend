package com.switchmate.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ExperienceResponse(
		UUID id,
		String company,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		String description) {
}