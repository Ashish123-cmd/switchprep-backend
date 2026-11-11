package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.switchmate.domain.entity.Education;
import com.switchmate.domain.entity.Experience;
import com.switchmate.domain.entity.Profile;
import com.switchmate.resume.ParsedEducation;
import com.switchmate.resume.ParsedExperience;
import com.switchmate.resume.ParsedProfile;

class SignupProfileMapperTest {

	private final SignupProfileMapper mapper = new SignupProfileMapper();

	@Test
	void appliesParsedProfileToProfile() {
		UUID userId = UUID.randomUUID();
		Profile profile = new Profile(userId);

		mapper.applyToProfile(profile, parsedProfile());

		assertEquals("+1 555 010 1234", profile.getPhone());
		assertEquals("Seattle, WA", profile.getLocation());
		assertEquals("Software Engineer", profile.getHeadline());
		assertEquals("Software engineer experienced in building reliable web applications.", profile.getSummary());
		assertEquals(5, profile.getTotalExperienceYears().intValue());
		assertEquals(List.of("Java", "Spring Boot"), List.copyOf(profile.getSkills()));
	}

	@Test
	void mapsParsedExperiencesForUser() {
		UUID userId = UUID.randomUUID();

		List<Experience> experiences = mapper.toExperiences(userId, parsedProfile());

		assertEquals(1, experiences.size());
		Experience experience = experiences.get(0);
		assertEquals(userId, experience.getProfileId());
		assertEquals("Example Company", experience.getCompany());
		assertEquals("Software Engineer", experience.getTitle());
		assertEquals(LocalDate.of(2021, 3, 1), experience.getStartDate());
		assertEquals(LocalDate.of(2024, 6, 30), experience.getEndDate());
		assertEquals("Built and maintained backend services.", experience.getDescription());
	}

	@Test
	void mapsParsedEducationsForUser() {
		UUID userId = UUID.randomUUID();

		List<Education> educations = mapper.toEducations(userId, parsedProfile());

		assertEquals(1, educations.size());
		Education education = educations.get(0);
		assertEquals(userId, education.getProfileId());
		assertEquals("Example University", education.getInstitution());
		assertEquals("BS", education.getDegree());
		assertEquals("Computer Science", education.getField());
		assertEquals(2016, education.getStartYear().intValue());
		assertEquals(2020, education.getEndYear().intValue());
	}

	@Test
	void skipsIncompleteCollectionsAndEntries() {
		UUID userId = UUID.randomUUID();
		Profile profile = new Profile(userId);
		ParsedProfile parsedProfile = new ParsedProfile(
				"Alex Morgan", null, null, null, null, null, null,
				List.of(new ParsedExperience("Example Company", "Software Engineer", null, null, null)),
				List.of(new ParsedEducation(null, "BS", "Computer Science", 2016, 2020)));

		mapper.applyToProfile(profile, parsedProfile);

		assertTrue(profile.getSkills().isEmpty());
		assertTrue(mapper.toExperiences(userId, parsedProfile).isEmpty());
		assertTrue(mapper.toEducations(userId, parsedProfile).isEmpty());
	}

	private ParsedProfile parsedProfile() {
		return new ParsedProfile(
				"Alex Morgan",
				"+1 555 010 1234",
				"Seattle, WA",
				"Software Engineer",
				"Software engineer experienced in building reliable web applications.",
				5,
				List.of("Java", "Spring Boot"),
				List.of(new ParsedExperience("Example Company", "Software Engineer",
						LocalDate.of(2021, 3, 1), LocalDate.of(2024, 6, 30),
						"Built and maintained backend services.")),
				List.of(new ParsedEducation("Example University", "BS", "Computer Science", 2016, 2020)));
	}
}