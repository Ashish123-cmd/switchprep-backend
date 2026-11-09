package com.switchmate.auth.dto;

import java.math.BigDecimal;
import java.util.Set;

public record JobPreferencesResponse(
		Integer minExperience,
		Integer maxExperience,
		Integer noticePeriodDays,
		BigDecimal expectedCtcLpa,
		String workAuthorization,
		Boolean openToRelocation,
		Set<String> preferredRoles,
		Set<String> preferredLocations) {
}