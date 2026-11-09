package com.switchmate.auth.dto;

import java.math.BigDecimal;
import java.util.Set;

import org.hibernate.validator.constraints.URL;

import com.switchmate.common.validation.ValidExperienceRange;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@ValidExperienceRange
public record JobPreferencesRequest(
		@PositiveOrZero Integer minExperience,
		@PositiveOrZero Integer maxExperience,
		@PositiveOrZero Integer noticePeriodDays,
		@DecimalMin("0.0") BigDecimal expectedCtcLpa,
		@Size(max = 255) String workAuthorization,
		Boolean openToRelocation,
		Set<@NotBlank @Size(max = 255) String> preferredRoles,
		Set<@NotBlank @Size(max = 255) String> preferredLocations) {
}