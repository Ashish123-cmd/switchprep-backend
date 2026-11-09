package com.switchmate.jobs.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.switchmate.domain.entity.Job;
import com.switchmate.domain.enums.JobSource;

class SkillAndExperienceScorerTest {

	@Test
	void scoresStrongSkillExperienceAndPreferenceFitHigher() {
		SkillAndExperienceScorer scorer = new SkillAndExperienceScorer(weights());
		UserProfile profile = new UserProfile("Backend Engineer", "Remote", 4, Set.of("Java", "SQL", "Docker"));
		JobPreferences preferences = new JobPreferences(Set.of("Engineer"), Set.of("Remote"));
		Job job = job("Backend Engineer", "Remote", Set.of("Java", "SQL"), 3, 5);

		MatchResult result = scorer.score(profile, preferences, job);

		assertEquals(100, result.score());
		assertEquals(3, result.reasons().size());
		assertTrue(result.reasons().get(0).contains("2 of 2"));
	}

	@Test
	void partialSkillsAndExperienceProduceLowerScore() {
		SkillAndExperienceScorer scorer = new SkillAndExperienceScorer(weights());
		UserProfile profile = new UserProfile(null, null, 1, Set.of("Java"));
		JobPreferences preferences = new JobPreferences(Set.of("Designer"), Set.of("London"));
		Job job = job("Backend Engineer", "Remote", Set.of("Java", "SQL"), 4, 6);

		MatchResult result = scorer.score(profile, preferences, job);

		assertTrue(result.score() < 60);
		assertEquals(3, result.reasons().size());
	}

	@Test
	void noJobSkillOrExperienceRequirementsRemainNeutralForThoseDimensions() {
		SkillAndExperienceScorer scorer = new SkillAndExperienceScorer(weights());
		UserProfile profile = new UserProfile(null, null, null, Set.of());
		Job job = job("Engineer", null, Set.of(), null, null);

		MatchResult result = scorer.score(profile, new JobPreferences(Set.of(), Set.of()), job);

		assertEquals(100, result.score());
	}

	private MatchScoringProperties weights() {
		MatchScoringProperties properties = new MatchScoringProperties();
		properties.setSkills(0.7);
		properties.setExperience(0.2);
		properties.setPreferences(0.1);
		return properties;
	}

	private Job job(String title, String location, Set<String> skills, Integer minExperience, Integer maxExperience) {
		Job job = new Job(JobSource.GREENHOUSE, UUID.randomUUID().toString());
		job.setTitle(title);
		job.setCompany("Example Co");
		job.setLocation(location);
		job.setSkills(skills);
		job.setMinExperience(minExperience);
		job.setMaxExperience(maxExperience);
		return job;
	}
}