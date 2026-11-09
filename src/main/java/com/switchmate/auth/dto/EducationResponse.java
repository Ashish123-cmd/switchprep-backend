package com.switchmate.auth.dto;

import java.util.UUID;

public record EducationResponse(
		UUID id,
		String institution,
		String degree,
		String field,
		Integer startYear,
		Integer endYear) {
}