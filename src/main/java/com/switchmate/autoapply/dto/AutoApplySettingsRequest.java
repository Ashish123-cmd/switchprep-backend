package com.switchmate.autoapply.dto;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AutoApplySettingsRequest(
		@NotNull Boolean enabled,
		@NotNull @Min(0) @Max(100) Integer minMatchScore,
		@NotNull @Min(1) Integer dailyCap,
		@Size(max = 200) Set<@NotBlank @Size(max = 255) String> excludedCompanies,
		Boolean requireApproval) {
}