package com.switchmate.applications;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.applications.dto.ApplicationResponse;
import com.switchmate.applications.dto.ApplicationView;
import com.switchmate.applications.dto.CompactJobResponse;
import com.switchmate.applications.dto.CreateApplicationRequest;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Application;
import com.switchmate.domain.entity.Job;
import com.switchmate.domain.enums.ApplicationStatus;
import com.switchmate.domain.enums.ApplicationMode;
import com.switchmate.repository.ApplicationRepository;
import com.switchmate.repository.JobRepository;
import com.switchmate.jobs.matching.UserJobStateService;
import com.switchmate.jobs.matching.UserJobStateView;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final JobRepository jobRepository;
	private final UserJobStateService userJobStateService;
	private final ApplicationStatusPolicy statusPolicy;

	public ApplicationService(
			ApplicationRepository applicationRepository,
			JobRepository jobRepository,
			UserJobStateService userJobStateService,
			ApplicationStatusPolicy statusPolicy) {
		this.applicationRepository = applicationRepository;
		this.jobRepository = jobRepository;
		this.userJobStateService = userJobStateService;
		this.statusPolicy = statusPolicy;
	}

	@Transactional
	public ApplicationView create(UUID userId, CreateApplicationRequest request) {
		Application application = createEntity(userId, request.jobId(), request.mode());
		Job job = findJob(application.getJobId());
		return toView(application, job, userJobStateService.findForUserAndJob(userId, job.getId()).saved());
	}

	@Transactional
	public Application createEntity(UUID userId, UUID jobId, ApplicationMode mode) {
		Job job = findJob(jobId);
		if (applicationRepository.existsByUserIdAndJobId(userId, jobId)) {
			throw new ConflictException("An application for this job already exists");
		}
		Application application = new Application(userId, job.getId(), ApplicationStatus.QUEUED, mode);
		try {
			return applicationRepository.saveAndFlush(application);
		} catch (DataIntegrityViolationException exception) {
			throw new ConflictException("An application for this job already exists");
		}
	}

	@Transactional(readOnly = true)
	public List<ApplicationView> list(UUID userId, ApplicationStatus status, ApplicationMode mode) {
		List<Application> applications = applicationRepository.findAllByUserId(userId).stream()
				.filter(application -> status == null || application.getStatus() == status)
				.filter(application -> mode == null || application.getMode() == mode)
				.toList();
		Map<UUID, Job> jobsById = loadJobs(applications.stream().map(Application::getJobId).collect(Collectors.toSet()));
		Map<UUID, UserJobStateView> stateByJobId = userJobStateService.findAllForUser(userId);
		return applications.stream()
				.map(application -> toView(application, requireJob(jobsById, application.getJobId()),
						stateByJobId.getOrDefault(application.getJobId(), new UserJobStateView(false, false)).saved()))
				.sorted(Comparator.comparing((ApplicationView view) -> view.application().updatedAt(),
						Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	@Transactional
	public ApplicationView updateStatus(UUID userId, UUID applicationId, ApplicationStatus status) {
		Application application = findOwnedApplication(userId, applicationId);
		statusPolicy.validate(application.getStatus(), status);
		application.setStatus(status);
		application = applicationRepository.saveAndFlush(application);
		Job job = findJob(application.getJobId());
		boolean saved = userJobStateService.findForUserAndJob(userId, job.getId()).saved();
		return toView(application, job, saved);
	}

	@Transactional
	public void delete(UUID userId, UUID applicationId) {
		Application application = findOwnedApplication(userId, applicationId);
		applicationRepository.delete(application);
	}

	@Transactional(readOnly = true)
	public List<com.switchmate.applications.dto.TrackerItem> tracker(UUID userId) {
		List<Application> applications = applicationRepository.findAllByUserId(userId);
		Set<UUID> applicationJobIds = applications.stream().map(Application::getJobId).collect(Collectors.toSet());
		Map<UUID, Job> applicationJobs = loadJobs(applicationJobIds);
		Map<UUID, UserJobStateView> states = userJobStateService.findAllForUser(userId);

		List<com.switchmate.applications.dto.TrackerItem> items = applications.stream()
				.map(application -> {
					Job job = requireJob(applicationJobs, application.getJobId());
					boolean saved = states.getOrDefault(job.getId(), new UserJobStateView(false, false)).saved();
					return new com.switchmate.applications.dto.TrackerItem(
							application.getId(), job.getId(), application.getStatus().name(), application.getMode(),
							application.getUpdatedAt(), saved, toCompactJob(job));
				})
				.collect(Collectors.toCollection(java.util.ArrayList::new));

		Set<UUID> savedOnlyJobIds = states.entrySet().stream()
				.filter(entry -> entry.getValue().saved() && !applicationJobIds.contains(entry.getKey()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		for (Job job : jobRepository.findAllById(savedOnlyJobIds)) {
			items.add(new com.switchmate.applications.dto.TrackerItem(
					null, job.getId(), "saved", null, null, true, toCompactJob(job)));
		}
		return items.stream()
				.sorted(Comparator.comparing((com.switchmate.applications.dto.TrackerItem item) -> item.updatedAt(),
						Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	private Application findOwnedApplication(UUID userId, UUID applicationId) {
		return applicationRepository.findByIdAndUserId(applicationId, userId)
				.orElseThrow(() -> new NotFoundException("Application not found"));
	}

	private Job findJob(UUID jobId) {
		return jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
	}

	private Map<UUID, Job> loadJobs(Set<UUID> jobIds) {
		return jobRepository.findAllById(jobIds).stream()
				.collect(Collectors.toMap(Job::getId, Function.identity()));
	}

	private Job requireJob(Map<UUID, Job> jobsById, UUID jobId) {
		Job job = jobsById.get(jobId);
		if (job == null) {
			throw new NotFoundException("Job not found");
		}
		return job;
	}

	private ApplicationView toView(Application application, Job job, boolean saved) {
		return new ApplicationView(
				new ApplicationResponse(application.getId(), application.getJobId(), application.getStatus(),
						application.getMode(), application.getUpdatedAt()),
				toCompactJob(job),
				saved);
	}

	private CompactJobResponse toCompactJob(Job job) {
		return new CompactJobResponse(job.getId(), job.getSource(), job.getTitle(), job.getCompany(),
				job.getLocation(), job.getApplyUrl(), job.getPostedAt());
	}
}