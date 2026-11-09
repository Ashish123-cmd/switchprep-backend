package com.switchmate.resume;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParsedEducation(
		@JsonProperty(value = "institution", required = true) String institution,
		@JsonProperty(value = "degree", required = true) String degree,
		@JsonProperty(value = "field", required = true) String field,
		@JsonProperty(value = "startYear", required = true) Integer startYear,
		@JsonProperty(value = "endYear", required = true) Integer endYear) {
}