package com.switchmate.jobs.ingestion;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.domain.entity.Job;
import com.switchmate.repository.JobRepository;

@Service
public class JobIngestionService {

	private static final Logger logger = LoggerFactory.getLogger(JobIngestionService.class);

	private final List<JobSource> sources;
	private final JobRepository jobRepository;

	public JobIngestionService(List<JobSource> sources, JobRepository jobRepository) {
		this.sources = sources;
		this.jobRepository = jobRepository;
	}

	@Transactional
	public JobIngestionSummary ingest() {
		int succeeded = 0;
		int failed = 0;
		int created = 0;
		int updated = 0;
		int deactivated = 0;
		for (JobSource source : sources) {
			try {
				List<RawJob> rawJobs = source.fetchJobs();
				UpsertCounts counts = upsertAndDeactivate(source, rawJobs, true);
				created += counts.created();
				updated += counts.updated();
				deactivated += counts.deactivated();
				succeeded++;
			} catch (IncompleteJobFetchException exception) {
				logger.warn("{}", exception.getMessage());
				try {
					UpsertCounts counts = upsertAndDeactivate(source, exception.fetchedJobs(), false);
					created += counts.created();
					updated += counts.updated();
					failed++;
				} catch (RuntimeException sourceException) {
					logger.error("Job ingestion failed for source {}", source.type(), sourceException);
					failed++;
				}
			} catch (RuntimeException exception) {
				logger.error("Job ingestion failed for source {}", source.type(), exception);
				failed++;
			}
		}
		return new JobIngestionSummary(succeeded, failed, created, updated, deactivated);
	}

	private UpsertCounts upsertAndDeactivate(JobSource source, List<RawJob> rawJobs, boolean completeFetch) {
		com.switchmate.domain.enums.JobSource persistedSource =
				com.switchmate.domain.enums.JobSource.valueOf(source.type().name());
		Set<String> returnedIds = new HashSet<>();
		int created = 0;
		int updated = 0;
		for (RawJob rawJob : rawJobs) {
			String externalId = normalize(rawJob.externalId());
			String title = normalize(rawJob.title());
			String company = normalize(rawJob.company());
			if (externalId == null || title == null || company == null) {
				logger.warn("Skipping incomplete job from source {}", source.type());
				continue;
			}
			returnedIds.add(externalId);
			Job job = jobRepository.findBySourceAndExternalId(persistedSource, externalId)
					.orElseGet(() -> new Job(persistedSource, externalId));
			boolean isNew = job.getId() == null;
			job.setTitle(title);
			job.setCompany(company);
			job.setLocation(normalize(rawJob.location()));
			job.setApplyUrl(normalize(rawJob.applyUrl()));
			job.setDescription(rawJob.description());
			job.setSkills(rawJob.skills() == null ? new LinkedHashSet<>() : normalizeSkills(rawJob.skills()));
			job.setMinExperience(rawJob.minExperience());
			job.setMaxExperience(rawJob.maxExperience());
			job.setPostedAt(rawJob.postedAt());
			job.setActive(true);
			jobRepository.save(job);
			if (isNew) {
				created++;
			} else {
				updated++;
			}
		}

		int deactivated = 0;
		if (completeFetch) {
			for (Job existing : jobRepository.findAllBySource(persistedSource)) {
				if (!returnedIds.contains(existing.getExternalId()) && existing.isActive()) {
					existing.setActive(false);
					jobRepository.save(existing);
					deactivated++;
				}
			}
		}
		return new UpsertCounts(created, updated, deactivated);
	}

	private LinkedHashSet<String> normalizeSkills(List<String> skills) {
		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		for (String skill : skills) {
			String value = normalize(skill);
			if (value != null) {
				normalized.add(value);
			}
		}
		return normalized;
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.strip().replaceAll("\\s+", " ");
	}

	private record UpsertCounts(int created, int updated, int deactivated) {
	}
}