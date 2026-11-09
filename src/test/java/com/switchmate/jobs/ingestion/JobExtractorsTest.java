package com.switchmate.jobs.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class JobExtractorsTest {

	@Test
	void keywordExtractorFindsConfiguredSkillsWithoutSubstringFalsePositives() {
		KeywordSkillExtractor extractor = new KeywordSkillExtractor(List.of("Java", "Spring Boot", "SQL"));

		var skills = extractor.extract("Senior Java Engineer", "Built APIs with Spring Boot and PostgreSQL.");

		assertEquals(List.of("Java", "Spring Boot"), List.copyOf(skills));
	}

	@Test
	void experienceExtractorHandlesMinimumAndRange() {
		ExperienceRangeExtractor extractor = new ExperienceRangeExtractor();

		assertEquals(new ExperienceRange(3, null), extractor.extract("3+ years of experience"));
		assertEquals(new ExperienceRange(2, 5), extractor.extract("2-5 years experience"));
		assertNull(extractor.extract("Experience preferred"));
	}
}