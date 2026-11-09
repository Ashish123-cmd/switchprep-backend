package com.switchmate.jobs.matching;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.UserJobState;
import com.switchmate.domain.entity.UserJobStateId;
import com.switchmate.repository.JobRepository;
import com.switchmate.repository.UserJobStateRepository;

@Service
public class UserJobStateService {

	private final UserJobStateRepository userJobStateRepository;
	private final JobRepository jobRepository;

	public UserJobStateService(UserJobStateRepository userJobStateRepository, JobRepository jobRepository) {
		this.userJobStateRepository = userJobStateRepository;
		this.jobRepository = jobRepository;
	}

	@Transactional(readOnly = true)
	public Map<UUID, UserJobStateView> findAllForUser(UUID userId) {
		return userJobStateRepository.findAllByIdUserId(userId).stream()
				.collect(Collectors.toMap(
						state -> state.getId().getJobId(),
						this::toView,
						(first, ignored) -> first));
	}

	@Transactional(readOnly = true)
	public UserJobStateView findForUserAndJob(UUID userId, UUID jobId) {
		return userJobStateRepository.findById(new UserJobStateId(userId, jobId))
				.map(this::toView)
				.orElseGet(() -> new UserJobStateView(false, false));
	}

	@Transactional
	public UserJobStateView setSaved(UUID userId, UUID jobId, boolean saved) {
		return update(userId, jobId, state -> state.setSaved(saved));
	}

	@Transactional
	public UserJobStateView setHidden(UUID userId, UUID jobId, boolean hidden) {
		return update(userId, jobId, state -> state.setHidden(hidden));
	}

	private UserJobStateView update(UUID userId, UUID jobId, java.util.function.Consumer<UserJobState> change) {
		if (!jobRepository.existsById(jobId)) {
			throw new NotFoundException("Job not found");
		}
		UserJobStateId stateId = new UserJobStateId(userId, jobId);
		UserJobState state = userJobStateRepository.findById(stateId).orElseGet(() -> new UserJobState(stateId));
		change.accept(state);
		return toView(userJobStateRepository.save(state));
	}

	private UserJobStateView toView(UserJobState state) {
		return new UserJobStateView(state.isSaved(), state.isHidden());
	}
}