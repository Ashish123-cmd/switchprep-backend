package com.switchmate.autoapply.dto;

import java.util.Set;

public record AutoApplySettingsResponse(
		boolean enabled,
		int minMatchScore,
		int dailyCap,
		Set<String> excludedCompanies,
		boolean requireApproval) {
}