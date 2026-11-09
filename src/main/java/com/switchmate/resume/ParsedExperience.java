package com.switchmate.resume;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParsedExperience(
		@JsonProperty(value = "company", required = true) String company,
		@JsonProperty(value = "title", required = true) String title,
		@JsonProperty(value = "startDate", required = true) LocalDate startDate,
		@JsonProperty(value = "endDate", required = true) LocalDate endDate,
		@JsonProperty(value = "description", required = true) String description) {
}