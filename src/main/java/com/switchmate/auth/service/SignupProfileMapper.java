package com.switchmate.auth.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.switchmate.domain.entity.Education;
import com.switchmate.domain.entity.Experience;
import com.switchmate.domain.entity.Profile;
import com.switchmate.resume.ParsedProfile;

@Component
public class SignupProfileMapper {

	public void applyToProfile(Profile profile, ParsedProfile parsedProfile) {
		profile.setPhone(parsedProfile.phone());
		profile.setLocation(parsedProfile.location());
		profile.setHeadline(parsedProfile.headline());
		profile.setSummary(parsedProfile.summary());
		profile.setTotalExperienceYears(parsedProfile.totalExperienceYears());
		profile.setSkills(toSkills(parsedProfile));
	}

	public List<Experience> toExperiences(UUID userId, ParsedProfile parsedProfile) {
		if (parsedProfile.experiences() == null) {
			return List.of();
		}
		return parsedProfile.experiences().stream()
				.filter(item -> item.company() != null && item.title() != null && item.startDate() != null)
				.map(item -> new Experience(
						userId,
						item.company(),
						item.title(),
						item.startDate(),
						item.endDate(),
						item.description()))
				.toList();
	}

	public List<Education> toEducations(UUID userId, ParsedProfile parsedProfile) {
		if (parsedProfile.educations() == null) {
			return List.of();
		}
		return parsedProfile.educations().stream()
				.filter(item -> item.institution() != null)
				.map(item -> new Education(
						userId,
						item.institution(),
						item.degree(),
						item.field(),
						item.startYear(),
						item.endYear()))
				.toList();
	}

	private Set<String> toSkills(ParsedProfile parsedProfile) {
		if (parsedProfile.skills() == null) {
			return new LinkedHashSet<>();
		}
		return parsedProfile.skills().stream()
				.filter(skill -> skill != null && !skill.isBlank())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}