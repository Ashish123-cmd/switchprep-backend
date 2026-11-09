package com.switchmate.applications;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Application;
import com.switchmate.domain.enums.ApplicationStatus;
import com.switchmate.jobs.matching.UserJobStateService;
import com.switchmate.repository.ApplicationRepository;
import com.switchmate.repository.JobRepository;

class ApplicationServiceTest {

	@Test
	void anotherUsersApplicationIsNotFoundAndNeverUpdated() {
		UUID userId = UUID.randomUUID();
		UUID applicationId = UUID.randomUUID();
		ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
		JobRepository jobRepository = mock(JobRepository.class);
		UserJobStateService stateService = mock(UserJobStateService.class);
		when(applicationRepository.findByIdAndUserId(applicationId, userId)).thenReturn(Optional.empty());
		ApplicationService service = new ApplicationService(
				applicationRepository, jobRepository, stateService, new ApplicationStatusPolicy());

		assertThrows(NotFoundException.class,
				() -> service.updateStatus(userId, applicationId, ApplicationStatus.APPLIED));
		verify(applicationRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any(Application.class));
	}
}