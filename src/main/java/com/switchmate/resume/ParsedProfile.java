package com.switchmate.resume;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParsedProfile(
		@JsonProperty(value = "name", required = true) String name,
		@JsonProperty(value = "phone", required = true) String phone,
		@JsonProperty(value = "location", required = true) String location,
		@JsonProperty(value = "headline", required = true) String headline,
		@JsonProperty(value = "summary", required = true) String summary,
		@JsonProperty(value = "totalExperienceYears", required = true) Integer totalExperienceYears,
		@JsonProperty(value = "skills", required = true) List<String> skills,
		@JsonProperty(value = "experiences", required = true) List<ParsedExperience> experiences,
		@JsonProperty(value = "educations", required = true) List<ParsedEducation> educations) {
}