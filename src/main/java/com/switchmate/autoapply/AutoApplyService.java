package com.switchmate.autoapply;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.applications.ApplicationService;
import com.switchmate.autoapply.dto.AutoApplyRunSummary;
import com.switchmate.autoapply.dto.AutoApplySettingsResponse;
import com.switchmate.domain.entity.Application;
import com.switchmate.domain.enums.ApplicationMode;
import com.switchmate.domain.enums.ApplicationStatus;
import com.switchmate.jobs.matching.JobSearchCriteria;
import com.switchmate.jobs.matching.MatchedJob;
import com.switchmate.jobs.matching.MatchedJobsService;
import com.switchmate.repository.ApplicationRepository;

@Service
public class AutoApplyService {

	private static final Logger logger = LoggerFactory.getLogger(AutoApplyService.class);

	private final AutoApplySettingsService settingsService;
	private final AutoApplySelector selector;
	private final MatchedJobsService matchedJobsService;
	private final ApplicationRepository applicationRepository;
	private final ApplicationService applicationService;
	private final ApplySubmitter applySubmitter;

	public AutoApplyService(
			AutoApplySettingsService settingsService,
			AutoApplySelector selector,
			MatchedJobsService matchedJobsService,
			ApplicationRepository applicationRepository,
			ApplicationService applicationService,
			ApplySubmitter applySubmitter) {
		this.settingsService = settingsService;
		this.selector = selector;
		this.matchedJobsService = matchedJobsService;
		this.applicationRepository = applicationRepository;
		this.applicationService = applicationService;
		this.applySubmitter = applySubmitter;
	}

	@Transactional
	public AutoApplyRunSummary run(UUID userId) {
		AutoApplySettingsResponse settings = settingsService.find(userId);
		if (!settings.enabled()) {
			return new AutoApplyRunSummary(0, 0, 0);
		}

		List<MatchedJob> feed = matchedJobsService.findMatchedJobs(userId,
				new JobSearchCriteria(null, Set.of(), null, null, null, null, false));
		List<com.switchmate.domain.entity.Application> applications = applicationRepository.findAllByUserId(userId);
		Set<UUID> appliedJobIds = applications.stream().map(Application::getJobId).collect(Collectors.toSet());
		List<AutoApplyCandidate> candidates = feed.stream()
				.map(matched -> new AutoApplyCandidate(matched.job().id(), matched.job().company(), matched.score(),
						true, false, appliedJobIds.contains(matched.job().id())))
				.toList();

		Instant startOfTodayUtc = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
		long applicationsCreatedToday = applicationRepository.countByUserIdAndModeAndCreatedAtGreaterThanEqual(
				userId, ApplicationMode.AUTO, startOfTodayUtc);
		int remainingSlots = (int) Math.max(0L, (long) settings.dailyCap() - applicationsCreatedToday);
		List<AutoApplyCandidate> selected = selector.select(
				true, candidates, settings.minMatchScore(), remainingSlots, settings.excludedCompanies());

		int queued = 0;
		int applied = 0;
		for (AutoApplyCandidate candidate : selected) {
			Application application = applicationService.createEntity(userId, candidate.jobId(), ApplicationMode.AUTO);
			if (settings.requireApproval()) {
				queued++;
				continue;
			}
			try {
				SubmissionResult result = applySubmitter.submit(application);
				if (result != null && result.success()) {
					applicationService.updateStatus(userId, application.getId(), ApplicationStatus.APPLIED);
					applied++;
				} else {
					logger.warn("Auto-apply submission failed for job {}: {}", candidate.jobId(),
							result == null ? "No submission result" : result.message());
					queued++;
				}
			} catch (RuntimeException exception) {
				logger.warn("Auto-apply submission failed for job {}", candidate.jobId(), exception);
				queued++;
			}
		}
		return new AutoApplyRunSummary(selected.size(), queued, applied);
	}
}