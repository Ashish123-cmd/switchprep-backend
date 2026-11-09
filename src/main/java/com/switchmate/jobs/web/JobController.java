package com.switchmate.jobs.web;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.domain.enums.JobSource;
import com.switchmate.jobs.matching.JobSearchCriteria;
import com.switchmate.jobs.matching.JobMatchDetails;
import com.switchmate.jobs.matching.MatchedJob;
import com.switchmate.jobs.matching.MatchedJobsService;
import com.switchmate.jobs.matching.UserJobStateService;
import com.switchmate.jobs.matching.UserJobStateView;
import com.switchmate.security.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Matched job feed, job details, and user job state")
public class JobController {

	private final MatchedJobsService matchedJobsService;
	private final UserJobStateService userJobStateService;

	public JobController(MatchedJobsService matchedJobsService, UserJobStateService userJobStateService) {
		this.matchedJobsService = matchedJobsService;
		this.userJobStateService = userJobStateService;
	}

	@GetMapping
	public List<MatchedJob> listJobs(
			@AuthenticationPrincipal CurrentUser currentUser,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Set<JobSource> source,
			@RequestParam(required = false) String location,
			@RequestParam(required = false) Integer minExperience,
			@RequestParam(required = false) Integer maxExperience,
			@RequestParam(required = false) Integer minScore,
			@RequestParam(defaultValue = "false") boolean savedOnly) {
		JobSearchCriteria criteria = new JobSearchCriteria(
				q, source, location, minExperience, maxExperience, minScore, savedOnly);
		return matchedJobsService.findMatchedJobs(currentUser.userId(), criteria);
	}

	@GetMapping("/{id}")
	public JobMatchDetails getJob(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID id) {
		return matchedJobsService.findMatchedJob(currentUser.userId(), id);
	}

	@PutMapping("/{id}/saved")
	public UserJobStateView setSaved(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID id,
			@Valid @RequestBody JobStateUpdateRequest request) {
		return userJobStateService.setSaved(currentUser.userId(), id, request.saved());
	}

	@PutMapping("/{id}/hidden")
	public UserJobStateView setHidden(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID id,
			@Valid @RequestBody HiddenStateUpdateRequest request) {
		return userJobStateService.setHidden(currentUser.userId(), id, request.hidden());
	}
}