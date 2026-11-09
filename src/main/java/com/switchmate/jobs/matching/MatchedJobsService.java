package com.switchmate.jobs.matching;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.JobPreferencesResponse;
import com.switchmate.auth.dto.UserProfileResponse;
import com.switchmate.auth.service.PreferencesService;
import com.switchmate.auth.service.ProfileService;
import com.switchmate.domain.entity.Job;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.repository.JobRepository;

@Service
public class MatchedJobsService {

	private final ProfileService profileService;
	private final PreferencesService preferencesService;
	private final JobRepository jobRepository;
	private final UserJobStateService userJobStateService;
	private final MatchScorer matchScorer;
	private final JobFilterChain jobFilterChain;
	private final SkillMatchHelper skillMatchHelper;

	public MatchedJobsService(
			ProfileService profileService,
			PreferencesService preferencesService,
			JobRepository jobRepository,
			UserJobStateService userJobStateService,
			MatchScorer matchScorer,
			JobFilterChain jobFilterChain,
			SkillMatchHelper skillMatchHelper) {
		this.profileService = profileService;
		this.preferencesService = preferencesService;
		this.jobRepository = jobRepository;
		this.userJobStateService = userJobStateService;
		this.matchScorer = matchScorer;
		this.jobFilterChain = jobFilterChain;
		this.skillMatchHelper = skillMatchHelper;
	}

	@Transactional(readOnly = true)
	public List<MatchedJob> findMatchedJobs(UUID userId, JobSearchCriteria criteria) {
		UserProfile profile = loadProfile(userId);
		JobPreferences preferences = loadPreferences(userId);
		Map<UUID, UserJobStateView> statesByJobId = userJobStateService.findAllForUser(userId);

		List<MatchedJob> matched = jobRepository.findAllByActiveTrue().stream()
			.filter(job -> {
				UserJobStateView state = statesByJobId.get(job.getId());
				return state == null || !state.hidden();
			})
			.map(job -> toMatchedJob(profile, preferences, job, statesByJobId.get(job.getId())))
			.toList();
		return jobFilterChain.apply(matched, criteria).stream()
				.sorted(Comparator.comparingInt(MatchedJob::score).reversed()
						.thenComparing(matchedJob -> matchedJob.job().postedAt(),
								Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	@Transactional(readOnly = true)
	public JobMatchDetails findMatchedJob(UUID userId, UUID jobId) {
		Job job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
		UserProfile profile = loadProfile(userId);
		JobPreferences preferences = loadPreferences(userId);
		UserJobStateView state = userJobStateService.findForUserAndJob(userId, jobId);
		MatchResult result = matchScorer.score(profile, preferences, job);
		SkillBreakdown skills = skillMatchHelper.compare(profile.skills(), job.getSkills());
		return new JobMatchDetails(toJobResponse(job), result.score(), result.reasons(), state.saved(), state.hidden(),
				skills.matchedSkills(), skills.missingSkills());
	}

	private MatchedJob toMatchedJob(
			UserProfile profile,
			JobPreferences preferences,
			Job job,
			UserJobStateView state) {
		MatchResult result = matchScorer.score(profile, preferences, job);
		return new MatchedJob(toJobResponse(job), result.score(), result.reasons(), state != null && state.saved());
	}

	private UserProfile loadProfile(UUID userId) {
		UserProfileResponse response = profileService.get(userId);
		return new UserProfile(response.headline(), response.location(), response.totalExperienceYears(), response.skills());
	}

	private JobPreferences loadPreferences(UUID userId) {
		JobPreferencesResponse response = preferencesService.get(userId);
		return new JobPreferences(response.preferredRoles(), response.preferredLocations());
	}

	private JobResponse toJobResponse(Job job) {
		return new JobResponse(job.getId(), job.getSource(), job.getExternalId(), job.getTitle(), job.getCompany(),
				job.getLocation(), job.getApplyUrl(), job.getDescription(), job.getMinExperience(), job.getMaxExperience(),
				job.getPostedAt(), Set.copyOf(job.getSkills()));
	}
}