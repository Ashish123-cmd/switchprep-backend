package com.switchmate.jobs.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.switchmate.domain.enums.JobSource;

class JobFiltersTest {

	private MatchedJob backend;
	private MatchedJob designer;
	private List<MatchedJob> jobs;

	@BeforeEach
	void setUp() {
		backend = matched("Backend Engineer", "Acme", "Remote", JobSource.GREENHOUSE,
				Set.of("Java", "PostgreSQL"), 3, 5, 85, true);
		designer = matched("Product Designer", "Northwind", "London", JobSource.LEVER,
				Set.of("Figma"), 2, 4, 70, false);
		jobs = List.of(backend, designer);
	}

	@Test
	void textFilterSearchesTitlesCompaniesAndSkills() {
		assertEquals(List.of(backend), new TextQueryJobFilter().filter(jobs, criteria("java")));
	}

	@Test
	void sourceFilterSupportsMultipleSources() {
		assertEquals(jobs, new SourceJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(JobSource.GREENHOUSE, JobSource.LEVER), null, null, null, null, false)));
	}

	@Test
	void locationFilterMatchesCaseInsensitively() {
		assertEquals(List.of(backend), new LocationJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(), "remote", null, null, null, false)));
	}

	@Test
	void minimumExperienceFilterKeepsOverlappingOrUnknownRanges() {
		assertEquals(List.of(backend), new MinExperienceJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(), null, 5, null, null, false)));
	}

	@Test
	void maximumExperienceFilterKeepsOverlappingOrUnknownRanges() {
		assertEquals(List.of(designer), new MaxExperienceJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(), null, null, 2, null, false)));
	}

	@Test
	void minimumScoreFilterDropsLowerScoringJobs() {
		assertEquals(List.of(backend), new MinScoreJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(), null, null, null, 80, false)));
	}

	@Test
	void savedOnlyFilterKeepsSavedJobs() {
		assertEquals(List.of(backend), new SavedOnlyJobFilter().filter(jobs, new JobSearchCriteria(
				null, Set.of(), null, null, null, null, true)));
	}

	private JobSearchCriteria criteria(String query) {
		return new JobSearchCriteria(query, Set.of(), null, null, null, null, false);
	}

	private MatchedJob matched(String title, String company, String location, JobSource source, Set<String> skills,
			Integer minExperience, Integer maxExperience, int score, boolean saved) {
		JobResponse job = new JobResponse(UUID.randomUUID(), source, "id", title, company, location, null, null,
				minExperience, maxExperience, null, skills);
		return new MatchedJob(job, score, List.of("reason"), saved);
	}
}