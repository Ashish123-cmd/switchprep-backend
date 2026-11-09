package com.switchmate.jobs.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.UserJobState;
import com.switchmate.domain.entity.UserJobStateId;
import com.switchmate.repository.JobRepository;
import com.switchmate.repository.UserJobStateRepository;

class UserJobStateServiceTest {

	@Test
	void repeatedSavedUpdateIsIdempotentAndPreservesHiddenFlag() {
		UUID userId = UUID.randomUUID();
		UUID jobId = UUID.randomUUID();
		UserJobStateRepository stateRepository = mock(UserJobStateRepository.class);
		JobRepository jobRepository = mock(JobRepository.class);
		when(jobRepository.existsById(jobId)).thenReturn(true);
		when(stateRepository.findById(any(UserJobStateId.class))).thenAnswer(invocation -> Optional.empty());
		when(stateRepository.save(any(UserJobState.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserJobStateService service = new UserJobStateService(stateRepository, jobRepository);

		assertEquals(new UserJobStateView(true, false), service.setSaved(userId, jobId, true));
		assertEquals(new UserJobStateView(true, false), service.setSaved(userId, jobId, true));
		verify(stateRepository, org.mockito.Mockito.times(2)).save(any(UserJobState.class));
	}

	@Test
	void updatingMissingJobDoesNotPersistAState() {
		UUID userId = UUID.randomUUID();
		UUID jobId = UUID.randomUUID();
		UserJobStateRepository stateRepository = mock(UserJobStateRepository.class);
		JobRepository jobRepository = mock(JobRepository.class);
		when(jobRepository.existsById(jobId)).thenReturn(false);
		UserJobStateService service = new UserJobStateService(stateRepository, jobRepository);

		org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class,
				() -> service.setHidden(userId, jobId, true));
		verify(stateRepository, never()).save(any(UserJobState.class));
	}
}