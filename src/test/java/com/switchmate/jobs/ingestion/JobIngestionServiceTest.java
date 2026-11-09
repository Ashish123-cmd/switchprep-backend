package com.switchmate.jobs.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import com.switchmate.domain.entity.Job;
import com.switchmate.domain.enums.JobSourceType;
import com.switchmate.repository.JobRepository;

class JobIngestionServiceTest {

	@Test
	void upsertsReturnedJobsAndDeactivatesMissingOnCompleteFetch() {
		JobRepository repository = mock(JobRepository.class);
		JobSource source = mock(JobSource.class);
		Job active = job("active-id", true);
		Job stale = job("stale-id", true);
		RawJob raw = new RawJob("active-id", "  Backend   Engineer ", " Example Co ", " Remote ",
				"https://example.test/jobs/1", "Description", List.of("Java"), 2, 5, Instant.parse("2025-01-01T00:00:00Z"));
		when(source.type()).thenReturn(JobSourceType.GREENHOUSE);
		when(source.fetchJobs()).thenReturn(List.of(raw));
		when(repository.findBySourceAndExternalId(com.switchmate.domain.enums.JobSource.GREENHOUSE, "active-id"))
				.thenReturn(Optional.of(active));
		when(repository.findAllBySource(com.switchmate.domain.enums.JobSource.GREENHOUSE))
				.thenReturn(List.of(active, stale));
		JobIngestionService service = new JobIngestionService(List.of(source), repository);

		JobIngestionSummary result = service.ingest();

		assertEquals(1, result.sourcesSucceeded());
		assertEquals(1, result.jobsUpdated());
		assertEquals(1, result.jobsDeactivated());
		verify(active).setTitle("Backend Engineer");
		verify(active).setCompany("Example Co");
		verify(active).setSkills(Set.of("Java"));
		verify(stale).setActive(false);
		verify(repository, org.mockito.Mockito.times(2)).save(any(Job.class));
	}

	@Test
	void partialSourceFailureUpsertsAvailableJobsWithoutDeactivating() {
		JobRepository repository = mock(JobRepository.class);
		JobSource source = mock(JobSource.class);
		RawJob raw = new RawJob("available-id", "Engineer", "Example Co", null, null, null,
				List.of(), null, null, null);
		when(source.type()).thenReturn(JobSourceType.GREENHOUSE);
		when(source.fetchJobs()).thenThrow(new IncompleteJobFetchException("one board failed", List.of(raw)));
		when(repository.findBySourceAndExternalId(
				com.switchmate.domain.enums.JobSource.GREENHOUSE, "available-id")).thenReturn(Optional.empty());
		JobIngestionService service = new JobIngestionService(List.of(source), repository);

		JobIngestionSummary result = service.ingest();

		assertEquals(1, result.sourcesFailed());
		assertEquals(1, result.jobsCreated());
		assertEquals(0, result.jobsDeactivated());
		verify(repository).save(any(Job.class));
		org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findAllBySource(any());
	}

	private Job job(String externalId, boolean active) {
		Job job = mock(Job.class);
		when(job.getId()).thenReturn(UUID.randomUUID());
		when(job.getExternalId()).thenReturn(externalId);
		when(job.isActive()).thenReturn(active);
		return job;
	}
}